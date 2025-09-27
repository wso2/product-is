package org.wso2.carbon.identity.test.integration.service.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        UserDTO user = new UserDTO();
        user.setUserID("u123");
        user.setUsername("alice");
        user.setPreferredUsername("ali");
        user.setDisplayName("Alice Wonderland");
        user.setTenantDomain("example.com");
        user.setUserStoreDomain("PRIMARY");

        Attribute[] attributes = { new Attribute("key1", "value1") };
        user.setAttributes(attributes);

        assertEquals("u123", user.getUserID());
        assertEquals("alice", user.getUsername());
        assertEquals("ali", user.getPreferredUsername());
        assertEquals("Alice Wonderland", user.getDisplayName());
        assertEquals("example.com", user.getTenantDomain());
        assertEquals("PRIMARY", user.getUserStoreDomain());
        assertEquals(attributes, user.getAttributes());
    }

    @Test
    void testSingleArgConstructor() {
        UserDTO user = new UserDTO("u001");
        assertEquals("u001", user.getUserID());
        assertNull(user.getUsername());
    }

    @Test
    void testThreeArgConstructor() {
        UserDTO user = new UserDTO("u002", "bob", "bobby");
        assertEquals("u002", user.getUserID());
        assertEquals("bob", user.getUsername());
        assertEquals("bobby", user.getPreferredUsername());
    }

    @Test
    void testFullArgConstructor() {
        Attribute[] attributes = { new Attribute("age", "25") };
        UserDTO user = new UserDTO("u003", "charlie", "chaz", "Charlie C",
                                   "wso2.com", "SECONDARY", attributes);

        assertEquals("u003", user.getUserID());
        assertEquals("charlie", user.getUsername());
        assertEquals("chaz", user.getPreferredUsername());
        assertEquals("Charlie C", user.getDisplayName());
        assertEquals("wso2.com", user.getTenantDomain());
        assertEquals("SECONDARY", user.getUserStoreDomain());
        assertEquals(attributes, user.getAttributes());
    }

    @Test
    void testGetDomainQualifiedUsername() {
        UserDTO user = new UserDTO();
        user.setUsername("david");
        user.setUserStoreDomain("SECONDARY");

        assertEquals("SECONDARY/david", user.getDomainQualifiedUsername());

        user.setUsername(null);
        assertNull(user.getDomainQualifiedUsername());
    }

    @Test
    void testGetFullQualifiedUsername() {
        UserDTO user = new UserDTO();
        user.setUsername("emma");
        user.setUserStoreDomain("PRIMARY");
        user.setTenantDomain("example.com");

        String fqName = user.getFullQualifiedUsername();
        assertEquals("PRIMARY/emma@example.com", fqName);
    }

    @Test
    void testEqualsAndHashCode() {
        UserDTO user1 = new UserDTO("u100", "frank", "f");
        user1.setUserStoreDomain("PRIMARY");
        user1.setTenantDomain("abc.com");

        UserDTO user2 = new UserDTO("u101", "frank", "franky");
        user2.setUserStoreDomain("PRIMARY");
        user2.setTenantDomain("abc.com");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testNotEquals() {
        UserDTO user1 = new UserDTO("u200", "george", "g");
        user1.setUserStoreDomain("PRIMARY");
        user1.setTenantDomain("abc.com");

        UserDTO user2 = new UserDTO("u201", "henry", "h");
        user2.setUserStoreDomain("SECONDARY");
        user2.setTenantDomain("abc.com");

        assertNotEquals(user1, user2);
    }
}
