/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.session.data.optimizer;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionSerializerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for GzipSessionSerializer.
 */
public class GzipSessionSerializerTest {

    private GzipSessionSerializer serializer;

    @BeforeMethod
    public void setUp() {
        serializer = new GzipSessionSerializer();
    }

    @Test
    public void testSerializeAndDeserialize() throws SessionSerializerException, IOException {
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("key1", "value1");
        sessionData.put("key2", "value2");

        InputStream serializedStream = serializer.serializeSessionObject(sessionData);
        Assert.assertNotNull(serializedStream);

        // Read stream to byte array to check content
        byte[] serializedData = readAllBytes(serializedStream);
        Assert.assertTrue(serializedData.length > 0);

        // Verify it is actually gzipped
        Assert.assertTrue(isGzipped(serializedData), "Serialized data should be GZIP compressed");

        // Deserialize
        InputStream deStream = new ByteArrayInputStream(serializedData);
        Object deserializedObj = serializer.deSerializeSessionObject(deStream);
        Assert.assertNotNull(deserializedObj);
        Assert.assertTrue(deserializedObj instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> deserializedData = (Map<String, String>) deserializedObj;
        Assert.assertEquals(deserializedData.get("key1"), "value1");
        Assert.assertEquals(deserializedData.get("key2"), "value2");
    }

    @Test
    public void testDeserializeUncompressedData() throws SessionSerializerException, IOException {
        // Create standard Java serialized data (not gzipped)
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("key1", "legacyValue");

        byte[] legacyData;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(sessionData);
            oos.flush();
            legacyData = baos.toByteArray();
        }

        // Verify it is NOT gzipped
        Assert.assertFalse(isGzipped(legacyData), "Legacy serialized data should not be GZIP compressed");

        // Deserialize using GzipSessionSerializer (fallback path)
        InputStream deStream = new ByteArrayInputStream(legacyData);
        Object deserializedObj = serializer.deSerializeSessionObject(deStream);
        Assert.assertNotNull(deserializedObj);
        Assert.assertTrue(deserializedObj instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> deserializedData = (Map<String, String>) deserializedObj;
        Assert.assertEquals(deserializedData.get("key1"), "legacyValue");
    }

    @Test
    public void testNullOrEmptyData() throws SessionSerializerException, IOException {
        InputStream nullOut = serializer.serializeSessionObject(null);
        Assert.assertNotNull(nullOut);
        Assert.assertEquals(readAllBytes(nullOut).length, 0);

        Assert.assertNull(serializer.deSerializeSessionObject(null));
        Assert.assertNull(serializer.deSerializeSessionObject(new ByteArrayInputStream(new byte[0])));
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    private boolean isGzipped(byte[] compressed) {
        return (compressed.length >= 2 &&
                compressed[0] == (byte) (0x1f) &&
                compressed[1] == (byte) (0x8b));
    }
}
