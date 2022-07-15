/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.utils;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapComparator;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.comparators.NormalizingComparator;
import org.apache.directory.api.ldap.model.schema.registries.AttributeTypeRegistry;
import org.apache.directory.api.ldap.model.schema.registries.ComparatorRegistry;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CoreSession;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.LdapPrincipal;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.kerberos.KerberosAttribute;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * External LDAP implementation for the LDAP user store related tests.
 */
public class ExternalLDAPServer {

    private DirectoryService directoryService;
    private LdapServer server;
    private String workingDirectoryPath;
    private String schemaZipFilePath;
    private int serverPort;

    public static final String ADMIN_PASSWORD_ALGORITHM = "SHA";

    public ExternalLDAPServer(String workingDirectory, String schemaZipFile, int serverPort) {

        this.workingDirectoryPath = workingDirectory;
        this.schemaZipFilePath = schemaZipFile;
        this.serverPort = serverPort;
    }

    public void init() throws Exception {

        initiateDirectoryService();
        initiateLDAPServer();
    }

    public void startServer(boolean addDefaultPartition) throws Exception {

        server.start();
        changeConnectionUserPassword("admin");
        if (addDefaultPartition) {
            addPartition(workingDirectoryPath);
        }
    }

    public void stopServer() throws Exception {

        server.stop();
    }

    private void initiateDirectoryService() throws Exception {

        directoryService = new DefaultDirectoryService();
        InstanceLayout instanceLayout = new InstanceLayout(workingDirectoryPath);
        directoryService.setInstanceLayout(instanceLayout);
        initiateSchema();
        directoryService.getChangeLog().setEnabled(false);
        directoryService.setDenormalizeOpAttrsEnabled(true);
        initSystemPartition();
        directoryService.startup();
    }

    private void initiateLDAPServer() {

        server = new LdapServer();
        server.setTransports(new TcpTransport(serverPort));
        server.setDirectoryService(directoryService);
    }

    private void initiateSchema() throws Exception {

        File schemaZipFile = new File(schemaZipFilePath);
        File schemaPartitionDirectory = new File(directoryService.getInstanceLayout().getPartitionsDirectory(),
                "schema");
        unzipSchemaFile(schemaZipFile, schemaPartitionDirectory);
        SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
        SchemaManager schemaManager = new DefaultSchemaManager(loader);
        schemaManager.loadAllEnabled();
        ComparatorRegistry comparatorRegistry = schemaManager.getComparatorRegistry();
        for (LdapComparator<?> comparator : comparatorRegistry) {
            if (comparator instanceof NormalizingComparator) {
                ((NormalizingComparator) comparator).setOnServer();
            }
        }
        directoryService.setSchemaManager(schemaManager);

        LdifPartition ldifPartition = new LdifPartition(directoryService.getSchemaManager(), directoryService
                .getDnFactory());
        ldifPartition.setId("schema");
        ldifPartition.setPartitionPath(new File(directoryService.getInstanceLayout().getPartitionsDirectory(),
                ldifPartition.getId()).toURI());
        SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
        schemaPartition.setWrappedPartition(ldifPartition);
        directoryService.setSchemaPartition(schemaPartition);
    }

    private void initSystemPartition() throws Exception {

        JdbmPartition systemPartition = new JdbmPartition(directoryService.getSchemaManager(), directoryService
                .getDnFactory());
        systemPartition.setId("system");
        systemPartition.setPartitionPath(new File(directoryService.getInstanceLayout().getPartitionsDirectory(),
                systemPartition.getId()).toURI());
        systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
        systemPartition.setSchemaManager(directoryService.getSchemaManager());

        Set indexedAttributes = new HashSet();
        indexedAttributes.add(new JdbmIndex(SchemaConstants.OBJECT_CLASS_AT, false));
        systemPartition.setIndexedAttributes(indexedAttributes);
        directoryService.setSystemPartition(systemPartition);
    }

    private void changeConnectionUserPassword(String password) throws Exception {

        if (this.directoryService != null) {
            CoreSession adminSession;
            try {
                adminSession = this.directoryService.getAdminSession();
            } catch (Exception e) {
                String msg = "An error occurred while retraining admin session.";
                throw new Exception(msg, e);
            }
            if (adminSession != null) {
                LdapPrincipal adminPrincipal = adminSession.getAuthenticatedPrincipal();
                if (adminPrincipal != null) {
                    String passwordToStore = "{" + ADMIN_PASSWORD_ALGORITHM + "}";
                    MessageDigest messageDigest;
                    try {
                        messageDigest = MessageDigest.getInstance(ADMIN_PASSWORD_ALGORITHM);
                    } catch (NoSuchAlgorithmException e) {
                        throw new Exception(
                                "Could not find digest algorithm - " + ADMIN_PASSWORD_ALGORITHM, e);
                    }
                    messageDigest.update(password.getBytes());
                    byte[] bytes = messageDigest.digest();
                    String hash = Base64.encode(bytes);
                    passwordToStore = passwordToStore + hash;
                    adminPrincipal.setUserPassword(passwordToStore.getBytes());
                    Attribute passwordAttribute = new DefaultAttribute(getAttributeType("userPassword"));
                    try {
                        passwordAttribute.add(passwordToStore.getBytes());
                    } catch (LdapInvalidAttributeValueException e) {
                        String msg = "Adding password attribute failed .";
                        throw new Exception(msg, e);
                    }

                    Modification serverModification = new DefaultModification();
                    serverModification.setOperation(ModificationOperation.REPLACE_ATTRIBUTE);
                    serverModification.setAttribute(passwordAttribute);
                    List<Modification> modifiedList = new ArrayList<>();
                    modifiedList.add(serverModification);
                    try {
                        adminSession.modify(adminPrincipal.getDn(), modifiedList);
                    } catch (Exception e) {
                        String msg = "Failed changing connection user password.";
                        throw new Exception(msg, e);
                    }
                } else {
                    String msg = "Could not retrieve admin principle. Failed changing connection " +
                            "user password.";
                    throw new Exception(msg);
                }
            } else {
                String msg = "Directory admin session is null. The LDAP server may not have " +
                        "started yet.";
                throw new Exception(msg);
            }
        } else {
            String msg = "Directory service is null. The LDAP server may not have started yet.";
            throw new Exception(msg);
        }
    }

    private AttributeType getAttributeType(String attributeName) throws Exception {

        if (this.directoryService != null) {
            SchemaManager schemaManager = this.directoryService.getSchemaManager();
            if (schemaManager != null) {
                AttributeTypeRegistry registry = schemaManager.getAttributeTypeRegistry();
                if (registry != null) {
                    try {
                        String oid = registry.getOidByName(attributeName);
                        return registry.lookup(oid);
                    } catch (LdapException e) {
                        String msg = "An error occurred while querying attribute " + attributeName +
                                " from registry.";
                        throw new Exception(msg, e);
                    }
                } else {
                    String msg = "Could not get attribute registry.";
                    throw new Exception(msg);
                }
            } else {
                String msg = "Cannot access schema manager. Directory server may not have started.";
                throw new Exception(msg);
            }
        } else {
            String msg = "The directory service is null. LDAP server might not have started.";
            throw new Exception(msg);
        }
    }

    private void addPartition(String workingDirectory) throws Exception {

        try {
            JdbmPartition partition = createNewPartition("root", "dc=WSO2,dc=ORG", workingDirectory);
            this.directoryService.addPartition(partition);
            CoreSession adminSession = this.directoryService.getAdminSession();

            if (!adminSession.exists(partition.getSuffixDn())) {
                List<String> objectClasses = new ArrayList<>();
                objectClasses.add("dcObject");
                objectClasses.add("extensibleObject");
                objectClasses.add("organization");
                objectClasses.add("top");
                addPartitionAttributes("dc=WSO2,dc=ORG", objectClasses, "WSO2.ORG", "WSO2");

                addUserStoreToPartition(partition.getSuffixDn().getName());
                addGroupStoreToPartition(partition.getSuffixDn().getName());
                addSharedGroupToPartition(partition.getSuffixDn().getName());

                List<String> objectClassesAdmin = new ArrayList<>();
                objectClassesAdmin.add("identityPerson");
                objectClassesAdmin.add("inetOrgPerson");
                objectClassesAdmin.add("top");
                objectClassesAdmin.add("organizationalPerson");
                objectClassesAdmin.add("wso2Person");
                objectClassesAdmin.add("scimPerson");
                objectClassesAdmin.add("person");
                addAdmin("admin", "Administrator", "admin",
                        "admin@wso2.com", "admin", "dc=WSO2,dc=ORG",
                        "WSO2.ORG", objectClassesAdmin);

                List<String> objectClassesAdminGroup = new ArrayList<>();
                objectClassesAdminGroup.add("groupOfNames");
                objectClassesAdminGroup.add("top");
                addAdminGroup("admin", "admin", "dc=WSO2,dc=ORG",
                        objectClassesAdminGroup);
                this.directoryService.sync();
            }

        } catch (Exception e) {
            String errorMessage = "Could not add the partition";
            throw new Exception(errorMessage, e);

        }
    }

    private JdbmPartition createNewPartition(String partitionId, String partitionSuffix, String workingDirectory)
            throws Exception {

        try {
            JdbmPartition partition = new JdbmPartition(directoryService.getSchemaManager(), directoryService
                    .getDnFactory());
            String partitionDirectoryName = workingDirectory + File.separator + partitionId;
            partition.setId(partitionId);
            partition.setSuffixDn(new Dn(partitionSuffix));
            partition.setPartitionPath(new File(partitionDirectoryName).toURI());

            Set<Index<?, String>> indexedAttrs = new HashSet<>();
            indexedAttrs.add(new JdbmIndex<Entry>("1.3.6.1.4.1.18060.0.4.1.2.3", true));
            indexedAttrs.add(new JdbmIndex<Entry>("1.3.6.1.4.1.18060.0.4.1.2.4", true));
            indexedAttrs.add(new JdbmIndex<Entry>("1.3.6.1.4.1.18060.0.4.1.2.5", true));
            indexedAttrs.add(new JdbmIndex<Entry>("1.3.6.1.4.1.18060.0.4.1.2.6", true));
            indexedAttrs.add(new JdbmIndex<Entry>("1.3.6.1.4.1.18060.0.4.1.2.7", true));
            indexedAttrs.add(new JdbmIndex<Entry>("ou", true));
            indexedAttrs.add(new JdbmIndex<Entry>("dc", true));
            indexedAttrs.add(new JdbmIndex<Entry>("objectClass", true));
            indexedAttrs.add(new JdbmIndex<Entry>("cn", true));
            indexedAttrs.add(new JdbmIndex<Entry>("uid", true));
            partition.setIndexedAttributes(indexedAttrs);

            return partition;
        } catch (LdapInvalidDnException e) {
            String msg = "Could not add a new partition with partition id " + partitionId +
                    " and suffix " + partitionSuffix;
            throw new Exception(msg, e);
        }
    }

    private void addPartitionAttributes(String partitionDN, List<String> objectClasses, String realm, String dc)
            throws Exception {

        try {
            Dn adminDN = new Dn(partitionDN);
            Entry serverEntry = this.directoryService.newEntry(adminDN);
            addObjectClasses(serverEntry, objectClasses);
            serverEntry.add("o", realm);
            if (dc == null) {
                System.out.println("Domain component not found for partition with DN - " + partitionDN +
                        ". Not setting domain component.");
            } else {
                serverEntry.add("dc", dc);
            }
            serverEntry.add("administrativeRole", "accessControlSpecificArea");
            this.directoryService.getAdminSession().add(serverEntry);
        } catch (Exception e) {
            String msg = "Could not add partition attributes for partition - " + partitionDN;
            throw new Exception(msg, e);
        }
    }

    private void addObjectClasses(Entry serverEntry, List<String> objectClasses) throws LdapException {

        for (String objectClass : objectClasses) {
            try {
                serverEntry.add("objectClass", objectClass);
            } catch (LdapException e) {
                throw new LdapException("Could not add class to partition " + serverEntry.getDn().getName(), e);
            }
        }
    }

    private void addUserStoreToPartition(String partitionSuffixDn) throws Exception {

        try {
            Dn usersDN = new Dn("ou=Users," + partitionSuffixDn);
            Entry usersEntry = this.directoryService.newEntry(usersDN);
            usersEntry.add("objectClass", "organizationalUnit", "top");
            usersEntry.add("ou", "Users");
            this.directoryService.getAdminSession().add(usersEntry);
        } catch (LdapInvalidDnException e) {
            String msg = "Could not add user store to partition - " + partitionSuffixDn +
                    ". Cause - partition domain name is not valid.";
            throw new Exception(msg, e);
        } catch (LdapException e) {
            String msg = "Could not add user store to partition - " + partitionSuffixDn;
            throw new Exception(msg, e);
        } catch (Exception e) {
            String msg = "Could not add user store to partition admin session. - " +
                    partitionSuffixDn;
            throw new Exception(msg, e);
        }
    }

    private void addGroupStoreToPartition(String partitionSuffixDn) throws Exception {

        Entry groupsEntry;
        try {
            Dn groupsDN = new Dn("ou=Groups," + partitionSuffixDn);
            groupsEntry = this.directoryService.newEntry(groupsDN);
            groupsEntry.add("objectClass", "organizationalUnit", "top");
            groupsEntry.add("ou", "Groups");
            this.directoryService.getAdminSession().add(groupsEntry);
        } catch (LdapException e) {
            String msg = "Could not add group store to partition - " + partitionSuffixDn;
            throw new Exception(msg, e);
        } catch (Exception e) {
            String msg = "Could not add group store to partition admin session. - " +
                    partitionSuffixDn;
            throw new Exception(msg, e);
        }
    }

    private void addSharedGroupToPartition(String partitionSuffixDn) throws Exception {

        Entry groupsEntry;
        try {
            Dn groupsDN = new Dn("ou=SharedGroups," + partitionSuffixDn);
            groupsEntry = this.directoryService.newEntry(groupsDN);
            groupsEntry.add("objectClass", "organizationalUnit", "top");
            groupsEntry.add("ou", "SharedGroups");
            this.directoryService.getAdminSession().add(groupsEntry);
        } catch (LdapException e) {
            String msg = "Could not add shared group store to partition - " + partitionSuffixDn;
            throw new Exception(msg, e);
        } catch (Exception e) {
            String msg = "Could not add shared group store to partition admin session. - " +
                    partitionSuffixDn;
            throw new Exception(msg, e);
        }
    }

    private void addAdminGroup(String adminRoleName, String adminUsername, String partitionSuffix,
                               List<String> objectClasses) throws Exception {

        if (adminRoleName != null && StringUtils.contains(adminRoleName, "/")) {
            String adminRole = adminRoleName;
            adminRole = adminRole.substring(adminRole.indexOf("/") + 1);
            adminRoleName = adminRole;
        }
        String domainName = "";
        try {
            if (adminRoleName != null) {
                domainName = "cn" + "=" + adminRoleName + "," + "ou=Groups," + partitionSuffix;
                Dn adminGroup = new Dn(domainName);
                Entry adminGroupEntry = directoryService.newEntry(adminGroup);
                addObjectClasses(adminGroupEntry, objectClasses);
                adminGroupEntry.add("cn", adminRoleName);
                adminGroupEntry.add("member", "uid" + "=" + adminUsername + "," + "ou=Users," +
                        partitionSuffix);
                directoryService.getAdminSession().add(adminGroupEntry);
            }
        } catch (LdapInvalidDnException e) {
            String msg = "Domain name invalid " + domainName;
            throw new Exception(msg, e);
        } catch (LdapException e) {
            throw new Exception("Could not add group entry - " + domainName, e);
        } catch (Exception e) {
            throw new Exception("Could not add group entry to admin session. DN - " +
                    domainName, e);
        }
    }

    private void addAdmin(String adminUsername, String adminLastName, String adminCommonName,
                          String adminEmail, String adminPassword, String partitionSuffix,
                          final String realm, List<String> objectClasses) throws Exception {

        if (adminUsername.contains("/")) {
            String admin = adminUsername;
            admin = admin.substring(admin.indexOf("/") + 1);
            adminUsername = admin;
        }
        String domainName = "uid" + "=" + adminUsername + "," + "ou=Users,"
                + partitionSuffix;
        try {
            Dn adminDn = new Dn(domainName);
            Entry adminEntry = directoryService.newEntry(adminDn);
            objectClasses.add("krb5principal");
            objectClasses.add("krb5kdcentry");
            addObjectClasses(adminEntry, objectClasses);

            adminEntry.add("uid", adminUsername);
            adminEntry.add("sn", adminLastName);
            adminEntry.add("givenName", adminCommonName);
            adminEntry.add("cn", adminUsername);
            adminEntry.add("mail", adminEmail);

            String principal = adminUsername + "/" + "carbon.super" + "@" + realm;
            adminEntry.put(KerberosAttribute.KRB5_PRINCIPAL_NAME_AT, principal);
            adminEntry.put(KerberosAttribute.KRB5_KEY_VERSION_NUMBER_AT, "0");

            PasswordAlgorithm passwordAlgorithm = PasswordAlgorithm.SHA;
            addAdminPassword(adminEntry, adminPassword, passwordAlgorithm);
            directoryService.getAdminSession().add(adminEntry);
        } catch (LdapInvalidDnException e) {
            throw new Exception("Domain name invalid " + domainName, e);
        } catch (LdapException e) {
            throw new Exception("Could not add entry to partition. DN - " + domainName, e);
        } catch (Exception e) {
            throw new Exception("Could not add group entry to admin session. DN - " + domainName, e);
        }
    }

    private void addAdminPassword(Entry adminEntry, String password, PasswordAlgorithm algorithm) throws Exception {

        try {
            String passwordToStore = "{" + algorithm.getAlgorithmName() + "}";
            if (algorithm != PasswordAlgorithm.PLAIN_TEXT) {
                MessageDigest md = MessageDigest.getInstance(algorithm.getAlgorithmName());
                md.update(password.getBytes());
                byte[] bytes = md.digest();
                String hash = Base64.encode(bytes);
                passwordToStore = passwordToStore + hash;
            } else {
                passwordToStore = password;
            }
            adminEntry.put("userPassword", passwordToStore.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Could not find matching hash algorithm - " + algorithm.getAlgorithmName(), e);
        }
    }

    private void unzipSchemaFile(File zipSchemaStore, File outputDirectory) throws IOException {

        ZipInputStream zipFileStream = null;
        try {
            FileInputStream schemaFileStream = new FileInputStream(zipSchemaStore);
            zipFileStream = new ZipInputStream(new BufferedInputStream(schemaFileStream));
            ZipEntry entry;
            while ((entry = zipFileStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File newDirectory = new File(outputDirectory, entry.getName());
                    if (!newDirectory.mkdirs()) {
                        throw new IOException("Unable to create directory - " + newDirectory.getAbsolutePath());
                    }
                    continue;
                }

                int size;
                byte[] buffer = new byte[2048];
                FileOutputStream extractedSchemaFile = new FileOutputStream(new File(outputDirectory, entry.getName()));
                BufferedOutputStream extractingBufferedStream =
                        new BufferedOutputStream(extractedSchemaFile, buffer.length);
                try {
                    while ((size = zipFileStream.read(buffer, 0, buffer.length)) != -1) {
                        extractingBufferedStream.write(buffer, 0, size);
                    }
                } finally {
                    if (extractingBufferedStream != null) {
                        extractingBufferedStream.flush();
                        extractingBufferedStream.close();
                    }
                }
            }
        } catch (IOException e) {
            String msg = "Unable to extract schema directory to location " +
                    outputDirectory.getAbsolutePath() + " from " +
                    zipSchemaStore.getAbsolutePath();
            throw new IOException(msg, e);
        } finally {
            if (zipFileStream != null) {
                zipFileStream.close();
            }
        }
    }
}
