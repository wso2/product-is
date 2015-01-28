package org.wso2.identity.integration.test.sts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ServerLogReader implements Runnable {
    private String streamType;
    private InputStream inputStream;
    private StringBuilder stringBuilder;
    private static final String STREAM_TYPE_IN = "inputStream";
    private static final String STREAM_TYPE_ERROR = "errorStream";
    private final Object lock = new Object();
    Thread thread;
    private volatile boolean running = true;
    private static final Log log = LogFactory.getLog(ServerLogReader.class);

    public ServerLogReader(String name, InputStream is) {
        this.streamType = name;
        this.inputStream = is;
        this.stringBuilder = new StringBuilder();
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public void run() {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while (running) {
                if (bufferedReader.ready()) {
                    String s = bufferedReader.readLine();
                    stringBuilder.setLength(0);
                    if (s == null) {
                        break;
                    }
                    if (STREAM_TYPE_IN.equals(streamType)) {
                        stringBuilder.append(s).append("\n");
                        log.info(s);
                    } else if (STREAM_TYPE_ERROR.equals(streamType)) {
                        stringBuilder.append(s).append("\n");
                        log.error(s);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Problem reading the [" + streamType + "] due to: " + ex.getMessage(), ex);
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing the server log stream: " + e.getMessage(), e);
                }
            }
        }
    }

    public String getOutput() {
        synchronized (lock) {
            return stringBuilder.toString();
        }
    }
}
