/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.lanes;

import org.wso2.identity.integration.test.utils.TestPorts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.net.URLClassLoader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Runs the integration test suite as parallel lanes on one machine. Called by Maven in the test phase.
 *
 * Each lane runs a group of testng.xml {@code <test>} blocks (from lanes.conf) in its own JVM, against its own
 * IS server. Every port a lane uses is shifted by {@code PORT_STEP x lane index}: the test code reads its ports
 * from {@link TestPorts}, and the runner gives each lane its own copy of the test resources with the
 * {@code @port.*@} tokens filled in and its own copy of the sample web apps with their ports moved. So lanes
 * share no port and no server state.
 *
 * With {@code -Dtest.lanes=1} the whole suite runs in one lane, which is the same as the old serial run.
 */
public final class TestLaneRunner {

    private static final String ALWAYS_ON = "is-tests-initialize";
    private static final int PORT_STEP = 1000;
    private static final String SYS_PREFIX = "lane.sys.";
    private static final String ENV_PREFIX = "lane.env.";
    private static final Pattern TEXT_FILE = Pattern.compile(".*\\.(toml|xml|json|properties)$");
    private static final Pattern WAR_TEXT_ENTRY =
            Pattern.compile(".*\\.(properties|xml|jsp|html|htm|js|json|txt|conf)$");
    private static final Pattern TOKEN = Pattern.compile("@(port\\.[a-z0-9.]+)@");
    private static final Pattern TEST_BLOCK = Pattern.compile("<test\\s+name=\"([^\"]+)\"([^>]*)>");

    /** Ports that appear as literals in prebuilt sample web apps (values at shift 0). */
    private static final int[] SAMPLE_APP_PORTS = {TestPorts.IS_HTTPS, TestPorts.IS_HTTP, TestPorts.IS2_HTTPS,
            TestPorts.IS2_HTTP, TestPorts.IS3_HTTPS, TestPorts.IS3_HTTP, TestPorts.TOMCAT};

    private final Path basedir;
    private final Path workDir;
    private final Path reportsDir;
    private final Path resourcesDir;
    private final Path testClassesDir;
    /** "true": keep every lane's IS packs and work dirs; "failed": keep them only for failed lanes. */
    private final String keep;

    private TestLaneRunner(Path basedir) {

        this.basedir = basedir;
        this.workDir = basedir.resolve("target/lanes");
        this.reportsDir = basedir.resolve("target/surefire-reports");
        this.resourcesDir = basedir.resolve("src/test/resources");
        this.testClassesDir = basedir.resolve("target/test-classes");
        this.keep = System.getProperty("lanes.keep", "false").trim();
    }

    public static void main(String[] args) throws Exception {

        if (Boolean.getBoolean("skipTests") || Boolean.getBoolean("maven.test.skip")) {
            System.out.println("[INFO] Tests are skipped.");
            return;
        }
        Path basedir = Paths.get(System.getProperty("lanes.basedir", ".")).toAbsolutePath().normalize();
        int failed = new TestLaneRunner(basedir).run();
        if (failed > 0 && !Boolean.getBoolean("maven.test.failure.ignore")) {
            throw new IllegalStateException("There are test failures in " + failed + " lane(s). See "
                    + basedir.resolve("target/lanes") + " for each lane's log.");
        }
    }

    private int run() throws Exception {

        String suiteText = new String(Files.readAllBytes(resourcesDir.resolve("testng.xml")),
                StandardCharsets.UTF_8);
        List<String> declaredBlocks = blocks(suiteText, false);
        List<String> activeBlocks = blocks(suiteText, true);
        Map<String, List<String>> lanes = lanes(activeBlocks, declaredBlocks);
        Properties ports = new Properties();
        try (InputStream in = Files.newInputStream(basedir.resolve("src/test/lanes/ports.properties"))) {
            ports.load(in);
        }
        String classpath = childClasspath();

        killProcessesUnder(workDir, "left over from an earlier run");
        deleteTree(workDir);
        deleteTree(reportsDir);
        Files.createDirectories(workDir);
        System.out.println("[INFO] Running integration tests in " + lanes.size() + " parallel lane(s). Logs: "
                + workDir);

        List<Lane> running = new ArrayList<>();
        // On Ctrl-C or an aborted build, stop every lane's process tree (test JVM, IS servers, ...).
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.forEach(Lane::stop);
            killProcessesUnder(workDir, "still running at shutdown");
        }));
        int index = 0;
        for (Map.Entry<String, List<String>> e : lanes.entrySet()) {
            Lane lane = new Lane(e.getKey(), index * PORT_STEP, e.getValue());
            prepare(lane, suiteText, activeBlocks, ports);
            lane.start(classpath);
            running.add(lane);
            System.out.println("[INFO] Started " + lane.name + " (ports +" + lane.shift + "): "
                    + String.join(", ", lane.blocks));
            index++;
        }

        long start = System.currentTimeMillis();
        while (running.stream().anyMatch(Lane::isAlive)) {
            for (Lane lane : running) {
                if (lane.process.waitFor(5, TimeUnit.SECONDS) && lane.endMillis == 0) {
                    lane.endMillis = System.currentTimeMillis();
                    finish(lane);
                    System.out.println("[INFO] " + lane.summaryLine(start));
                }
            }
        }
        for (Lane lane : running) {
            if (lane.endMillis == 0) {
                lane.endMillis = System.currentTimeMillis();
                finish(lane);
                System.out.println("[INFO] " + lane.summaryLine(start));
            }
        }
        return report(running, start);
    }

    /** Reads lanes.conf, or puts every block in one lane when -Dtest.lanes=1. */
    private Map<String, List<String>> lanes(List<String> activeBlocks, List<String> declaredBlocks)
            throws IOException {

        Map<String, List<String>> lanes = new LinkedHashMap<>();
        if ("1".equals(System.getProperty("test.lanes"))) {
            List<String> all = new ArrayList<>(activeBlocks);
            all.remove(ALWAYS_ON);
            lanes.put("lane1", all);
            return lanes;
        }
        Path file = Paths.get(System.getProperty("test.lanes.file",
                basedir.resolve("src/test/lanes/lanes.conf").toString()));
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            line = line.replaceAll("#.*", "").trim();
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            List<String> blocks = Stream.of(line.substring(colon + 1).split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            lanes.put(line.substring(0, colon).trim(), blocks);
        }
        // Every active block must be in exactly one lane, so a new block is never skipped silently.
        List<String> problems = new ArrayList<>();
        for (String block : activeBlocks) {
            long n = lanes.values().stream().filter(b -> b.contains(block)).count();
            if (n == 0 && Boolean.getBoolean("lanes.allowPartial")) {
                continue; // Trial runs only: blocks in no lane are skipped.
            }
            if (!ALWAYS_ON.equals(block) && n != 1) {
                problems.add("block '" + block + "' is in " + n + " lanes");
            }
        }
        lanes.values().stream().flatMap(List::stream).filter(b -> !declaredBlocks.contains(b))
                .forEach(b -> problems.add("lane block '" + b + "' is not a block in testng.xml"));
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Invalid " + file + ": " + String.join("; ", problems));
        }
        // Blocks disabled in testng.xml (enabled="false", e.g. by a CI script that runs a subset) are dropped,
        // and so are lanes left with nothing to run.
        lanes.values().forEach(b -> b.retainAll(activeBlocks));
        lanes.values().removeIf(List::isEmpty);
        return fitToMachine(lanes);
    }

    /**
     * Merges lanes so that they fit this machine: each lane needs about {@code lanes.memoryPerLaneMb} of memory
     * (test JVM, IS servers, Tomcat) and {@code lanes.coresPerLane} CPU cores, after {@code lanes.reserveMb} is kept
     * for Maven and the OS. Lanes are merged round-robin, which keeps them balanced because lanes.conf lanes are
     * balanced. {@code -Dtest.lanes=N} sets the number of lanes directly.
     */
    private static Map<String, List<String>> fitToMachine(Map<String, List<String>> lanes) {

        int cores = Runtime.getRuntime().availableProcessors();
        long availableMb = availableMemoryMb();
        int memoryPerLane = Integer.getInteger("lanes.memoryPerLaneMb", 3500);
        int coresPerLane = Integer.getInteger("lanes.coresPerLane", 2);
        int reserveMb = Integer.getInteger("lanes.reserveMb", 1500);
        int byCpu = Math.max(1, cores / coresPerLane);
        int byMemory = availableMb < 0 ? lanes.size() : (int) Math.max(1, (availableMb - reserveMb) / memoryPerLane);
        String forced = System.getProperty("test.lanes", "").trim();
        int target = forced.matches("\\d+") && Integer.parseInt(forced) > 0
                ? Integer.parseInt(forced) : Math.min(lanes.size(), Math.min(byCpu, byMemory));
        System.out.println("[INFO] Machine: " + cores + " cores, " + availableMb + " MB memory available. Lanes: "
                + lanes.size() + " configured, " + target + " used" + (forced.isEmpty() ? "" : " (-Dtest.lanes)")
                + ".");
        if (target >= lanes.size()) {
            return lanes;
        }
        List<List<String>> merged = new ArrayList<>();
        for (int i = 0; i < target; i++) {
            merged.add(new ArrayList<>());
        }
        int i = 0;
        for (List<String> blocks : lanes.values()) {
            merged.get(i++ % target).addAll(blocks);
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (int n = 0; n < target; n++) {
            result.put("lane" + (n + 1), merged.get(n));
        }
        return result;
    }

    /** MemAvailable from /proc/meminfo (Linux), or the JVM's view of free physical memory; -1 if unknown. */
    private static long availableMemoryMb() {

        try {
            for (String line : Files.readAllLines(Paths.get("/proc/meminfo"))) {
                if (line.startsWith("MemAvailable:")) {
                    return Long.parseLong(line.replaceAll("\\D+", "")) / 1024;
                }
            }
        } catch (Exception ignored) {
            // Not Linux.
        }
        java.lang.management.OperatingSystemMXBean os = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) os).getFreeMemorySize() / (1024 * 1024);
        }
        return -1;
    }

    /** The {@code <test>} blocks in testng.xml outside comments; only enabled ones if {@code activeOnly}. */
    private static List<String> blocks(String suiteText, boolean activeOnly) {

        String noComments = suiteText.replaceAll("(?s)<!--.*?-->", "");
        List<String> blocks = new ArrayList<>();
        Matcher m = TEST_BLOCK.matcher(noComments);
        while (m.find()) {
            if (!activeOnly || !m.group(2).matches("(?s).*\\benabled\\s*=\\s*\"false\".*")) {
                blocks.add(m.group(1));
            }
        }
        return blocks;
    }

    /** Builds the lane's suite file, resources (tokens filled in) and sample web apps (ports moved). */
    private void prepare(Lane lane, String suiteText, List<String> activeBlocks, Properties ports)
            throws IOException {

        // The lane's target dir links to the module's build output (coverage agent, test classes, ...),
        // and holds the lane's own extracted IS packs.
        Path laneTarget = Files.createDirectories(lane.dir.resolve("target"));
        try (Stream<Path> entries = Files.list(basedir.resolve("target"))) {
            for (Path entry : (Iterable<Path>) entries::iterator) {
                String n = entry.getFileName().toString();
                if (!n.equals("lanes") && !n.equals("surefire-reports") && !n.startsWith("carbontmp")) {
                    Files.createSymbolicLink(laneTarget.resolve(n), entry);
                }
            }
        }
        String suite = suiteText;
        for (String block : activeBlocks) {
            if (!ALWAYS_ON.equals(block) && !lane.blocks.contains(block)) {
                suite = suite.replace("<test name=\"" + block + "\"", "<test name=\"" + block + "\" enabled=\"false\"");
            }
        }
        Files.write(lane.dir.resolve("testng.xml"), suite.getBytes(StandardCharsets.UTF_8));

        Map<String, String> values = new LinkedHashMap<>();
        for (String key : ports.stringPropertyNames()) {
            values.put(key, String.valueOf(Integer.parseInt(ports.getProperty(key).trim()) + lane.shift));
        }
        try (Stream<Path> files = Files.walk(resourcesDir)) {
            for (Path src : (Iterable<Path>) files::iterator) {
                Path dest = lane.resources.resolve(resourcesDir.relativize(src).toString());
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else if (TEXT_FILE.matcher(src.getFileName().toString()).matches()) {
                    String text = new String(Files.readAllBytes(src), StandardCharsets.UTF_8);
                    Matcher m = TOKEN.matcher(text);
                    StringBuffer out = new StringBuffer();
                    while (m.find()) {
                        String v = values.get(m.group(1));
                        m.appendReplacement(out, Matcher.quoteReplacement(v != null ? v : m.group()));
                    }
                    m.appendTail(out);
                    Files.write(dest, out.toString().getBytes(StandardCharsets.UTF_8));
                } else {
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        // The sample web apps are built once with the default ports; give shifted lanes their own copies.
        Path samples = testClassesDir.resolve("samples");
        if (lane.shift != 0 && Files.isDirectory(samples)) {
            Files.createDirectories(lane.resources.resolve("samples"));
            try (Stream<Path> wars = Files.list(samples)) {
                for (Path war : (Iterable<Path>) wars.filter(p -> p.toString().endsWith(".war"))::iterator) {
                    shiftWarPorts(war, lane.resources.resolve("samples").resolve(war.getFileName()), lane.shift);
                }
            }
        }
    }

    private static void shiftWarPorts(Path in, Path out, int shift) throws IOException {

        String alternatives = Arrays.stream(SAMPLE_APP_PORTS).mapToObj(String::valueOf)
                .collect(Collectors.joining("|"));
        Pattern port = Pattern.compile("(?<![\\w.])(" + alternatives + ")(?![\\w.])");
        try (ZipFile zip = new ZipFile(in.toFile());
             ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(out))) {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                byte[] data = readAll(zip.getInputStream(entry));
                if (!entry.isDirectory() && WAR_TEXT_ENTRY.matcher(entry.getName()).matches()) {
                    String text = decodeUtf8(data);
                    if (text != null) {
                        Matcher m = port.matcher(text);
                        StringBuffer sb = new StringBuffer();
                        while (m.find()) {
                            m.appendReplacement(sb, String.valueOf(Integer.parseInt(m.group(1)) + shift));
                        }
                        m.appendTail(sb);
                        data = sb.toString().getBytes(StandardCharsets.UTF_8);
                    }
                }
                ZipEntry copy = new ZipEntry(entry.getName());
                copy.setTime(entry.getTime());
                zos.putNextEntry(copy);
                zos.write(data);
                zos.closeEntry();
            }
        }
    }

    /** The test classpath from the class loader Maven gave us, minus jars surefire also excluded. */
    private static String childClasspath() {

        // Only the loader Maven created for the test classpath; its parents are Maven's own.
        Set<String> entries = new LinkedHashSet<>();
        ClassLoader cl = TestLaneRunner.class.getClassLoader();
        if (!(cl instanceof URLClassLoader)) {
            throw new IllegalStateException("Run TestLaneRunner through Maven (exec:java with the test classpath).");
        }
        for (URL url : ((URLClassLoader) cl).getURLs()) {
            try {
                entries.add(Paths.get(url.toURI()).toString());
            } catch (Exception e) {
                entries.add(url.getPath());
            }
        }
        return entries.stream().filter(e -> !e.contains("pax-logging-api"))
                .collect(Collectors.joining(File.pathSeparator));
    }

    /**
     * Collects the lane's results and keeps each IS server's logs. Unless -Dlanes.keep=true, it then deletes what
     * the lane no longer needs: extracted IS packs, the embedded Tomcat's work dir, the embedded LDAP's data and
     * the lane's copy of the test resources. Reports, the lane log and the server logs stay.
     */
    private void finish(Lane lane) throws IOException {

        lane.readResults();
        // A server the lane started may outlive its test JVM (e.g. after a failed shutdown). Stop it before its
        // files are deleted, and so it doesn't hold the lane's ports.
        killProcessesUnder(lane.dir, "left running by " + lane.name);
        Path laneTarget = lane.dir.resolve("target");
        List<Path> scratch = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(laneTarget)) {
            for (Path p : (Iterable<Path>) dirs::iterator) {
                String n = p.getFileName().toString();
                if (Files.isSymbolicLink(p)) {
                    continue;
                }
                if (n.startsWith("carbontmp")) {
                    try (Stream<Path> logDirs = Files.walk(p, 3)) {
                        for (Path logs : (Iterable<Path>) logDirs.filter(d -> d.endsWith("repository/logs"))
                                ::iterator) {
                            copyTree(logs, lane.dir.resolve("server-logs").resolve(n));
                        }
                    }
                    scratch.add(p);
                } else if (n.startsWith("tomcat.") || n.equals("ldapWorkingDirectory")) {
                    scratch.add(p);
                }
            }
        }
        boolean keepFiles = "true".equalsIgnoreCase(keep) || ("failed".equalsIgnoreCase(keep) && !lane.passed());
        if (!keepFiles) {
            for (Path p : scratch) {
                deleteTree(p);
            }
            deleteTree(lane.dir.resolve("src"));
        }
    }

    private int report(List<Lane> lanes, long start) {

        int total = 0;
        int failures = 0;
        int skipped = 0;
        int failedLanes = 0;
        System.out.println("[INFO] ");
        System.out.println("[INFO] Results:");
        System.out.println("[INFO] ");
        for (Lane lane : lanes) {
            System.out.println("[INFO] " + lane.summaryLine(start));
            total += lane.total;
            failures += lane.failed;
            skipped += lane.skipped;
            if (!lane.passed()) {
                failedLanes++;
            }
        }
        System.out.println("[INFO] ");
        System.out.println((failures > 0 || failedLanes > 0 ? "[ERROR] " : "[INFO] ") + "Tests run: " + total
                + ", Failures: " + failures + ", Errors: 0, Skipped: " + skipped);
        System.out.println("[INFO] ");
        return failedLanes;
    }

    private static String decodeUtf8(byte[] data) {

        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {

        try (InputStream is = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    private static void copyTree(Path from, Path to) throws IOException {

        try (Stream<Path> files = Files.walk(from)) {
            for (Path src : (Iterable<Path>) files::iterator) {
                Path dest = to.resolve(from.relativize(src).toString());
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void deleteTree(Path dir) throws IOException {

        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) files.sorted(Comparator.reverseOrder())::iterator) {
                Files.deleteIfExists(p);
            }
        }
    }

    /**
     * Stops a process and everything it started: children first (so none get reparented and keep running), then
     * the process itself. Whatever is still alive after 30 seconds is killed.
     */
    private static void stopTree(ProcessHandle root) {

        List<ProcessHandle> tree = root.descendants().collect(Collectors.toList());
        Collections.reverse(tree);
        tree.add(root);
        tree.forEach(ProcessHandle::destroy);
        long deadline = System.currentTimeMillis() + 30_000;
        for (ProcessHandle p : tree) {
            try {
                p.onExit().get(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                p.destroyForcibly();
            }
        }
    }

    /**
     * Stops every process whose command line refers to a path under {@code dir}: IS servers (their carbon.home is in a
     * lane's work dir), lane test JVMs and the processes they started.
     */
    private static void killProcessesUnder(Path dir, String why) {

        // Trailing separator, so ".../lane1/" doesn't also match ".../lane10/".
        String marker = dir.toAbsolutePath().normalize() + File.separator;
        long self = ProcessHandle.current().pid();
        List<ProcessHandle> found = ProcessHandle.allProcesses()
                .filter(p -> p.pid() != self)
                .filter(p -> p.info().commandLine().map(c -> c.contains(marker)).orElse(false))
                .collect(Collectors.toList());
        for (ProcessHandle p : found) {
            System.out.println("[WARNING] Stopping process " + p.pid() + ", " + why + ": "
                    + p.info().command().orElse("?"));
            stopTree(p);
        }
    }

    /** One lane: its JVM process and results. */
    private final class Lane {

        final String name;
        final int shift;
        final List<String> blocks;
        final Path dir;
        final Path resources;
        final Path reports;
        Process process;
        long endMillis;
        int total;
        int failed;
        int skipped;
        boolean hasResults;

        Lane(String name, int shift, List<String> blocks) {

            this.name = name;
            this.shift = shift;
            this.blocks = blocks;
            this.dir = workDir.resolve(name);
            this.resources = dir.resolve("src/test/resources");
            this.reports = reportsDir.resolve(name);
        }

        void start(String classpath) throws IOException {

            List<String> cmd = new ArrayList<>();
            cmd.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
            String jvmArgs = System.getProperty("lanes.jvmArgs", "").trim();
            if (!jvmArgs.isEmpty()) {
                cmd.addAll(Arrays.asList(jvmArgs.split("\\s+")));
            }
            for (String key : System.getProperties().stringPropertyNames()) {
                if (key.startsWith(SYS_PREFIX)) {
                    cmd.add("-D" + key.substring(SYS_PREFIX.length()) + "=" + System.getProperty(key).trim());
                }
            }
            cmd.add("-Dbasedir=" + dir);
            cmd.add("-Dframework.resource.location=" + resources + File.separator);
            cmd.add("-Dlane.port.shift=" + shift);
            cmd.add("-cp");
            cmd.add(resources + File.pathSeparator + classpath);
            cmd.add("org.testng.TestNG");
            cmd.add("-d");
            cmd.add(reports.toString());
            cmd.add("-usedefaultlisteners");
            cmd.add("false");
            cmd.add("-listener");
            cmd.add("org.testng.reporters.XMLReporter,org.testng.reporters.JUnitReportReporter");
            cmd.add(dir.resolve("testng.xml").toString());

            ProcessBuilder pb = new ProcessBuilder(cmd).directory(dir.resolve("target").toFile())
                    .redirectErrorStream(true).redirectOutput(dir.resolve("lane.log").toFile());
            Map<String, String> env = pb.environment();
            for (String key : System.getProperties().stringPropertyNames()) {
                if (key.startsWith(ENV_PREFIX)) {
                    // "@lane@" gives each lane its own value, e.g. its own database name.
                    env.put(key.substring(ENV_PREFIX.length()), System.getProperty(key).trim().replace("@lane@", name));
                }
            }
            String mavenHome = System.getProperty("lanes.mavenHome");
            if (mavenHome != null) {
                // Some tests run mvn from a shell script (adaptive.sh), so mvn must be on the PATH.
                env.put("PATH", Paths.get(mavenHome, "bin") + File.pathSeparator + env.getOrDefault("PATH", ""));
            }
            Files.createDirectories(reports);
            process = pb.start();
        }

        boolean isAlive() {

            return process.isAlive();
        }

        void stop() {

            if (process != null) {
                stopTree(process.toHandle());
            }
        }

        void readResults() {

            Path results = reports.resolve("testng-results.xml");
            if (!Files.exists(results)) {
                return;
            }
            try {
                String head = new String(Files.readAllBytes(results), StandardCharsets.UTF_8);
                Matcher m = Pattern.compile("<testng-results([^>]*)>").matcher(head);
                if (m.find()) {
                    total = attr(m.group(1), "total");
                    failed = attr(m.group(1), "failed");
                    skipped = attr(m.group(1), "skipped");
                    hasResults = true;
                }
            } catch (IOException e) {
                hasResults = false;
            }
        }

        boolean passed() {

            return hasResults && failed == 0;
        }

        String summaryLine(long start) {

            long secs = (endMillis - start) / 1000;
            String result = hasResults ? "Tests run: " + total + ", Failures: " + failed + ", Skipped: " + skipped
                    : "no results (did the lane fail to start? see " + dir.resolve("lane.log") + ")";
            return String.format("%-8s %s  %3dm%02ds  %s", name, passed() ? "PASSED" : "FAILED", secs / 60,
                    secs % 60, result);
        }
    }

    private static int attr(String attrs, String name) {

        Matcher m = Pattern.compile(name + "=\"(\\d+)\"").matcher(attrs);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
}
