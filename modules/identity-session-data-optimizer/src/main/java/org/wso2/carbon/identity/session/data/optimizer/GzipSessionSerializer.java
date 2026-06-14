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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionSerializerException;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GzipSessionSerializer implements data level optimization for WSO2 Identity Server session persistence.
 * It serializes session context objects using Java Serialization and compresses the output using GZIP.
 * On deserialization, it checks for GZIP magic headers, ensuring backward compatibility with uncompressed data.
 */
@Component(
        name = "org.wso2.carbon.identity.session.data.optimizer.GzipSessionSerializer",
        service = SessionSerializer.class,
        immediate = true,
        property = {
                "service.ranking:Integer=100"
        }
)
public class GzipSessionSerializer implements SessionSerializer {

    private static final Log log = LogFactory.getLog(GzipSessionSerializer.class);

    @Override
    public java.io.InputStream serializeSessionObject(Object obj) throws SessionSerializerException {
        if (obj == null) {
            return new ByteArrayInputStream(new byte[0]);
        }
        if (log.isDebugEnabled()) {
            log.debug("Serializing and compressing session object using GzipSessionSerializer");
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzos = new GZIPOutputStream(baos);
             ObjectOutputStream oos = new ObjectOutputStream(gzos)) {
            oos.writeObject(obj);
            oos.flush();
            gzos.finish();
            byte[] compressedData = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Session object compressed successfully. Compressed size: " + compressedData.length + " bytes.");
            }
            return new ByteArrayInputStream(compressedData);
        } catch (IOException e) {
            throw new SessionSerializerException("Error while serializing and compressing session object", e);
        }
    }

    @Override
    public Object deSerializeSessionObject(java.io.InputStream inputStream) throws SessionSerializerException {
        if (inputStream == null) {
            return null;
        }
        try {
            byte[] data;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                data = baos.toByteArray();
            }
            if (data.length == 0) {
                return null;
            }
            if (isGzipped(data)) {
                if (log.isDebugEnabled()) {
                    log.debug("Deserializing GZIP compressed session data");
                }
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                     GZIPInputStream gzis = new GZIPInputStream(bais);
                     ObjectInputStream ois = new ObjectInputStream(gzis)) {
                    return ois.readObject();
                } catch (ClassNotFoundException e) {
                    throw new SessionSerializerException("Class not found while decompressing and deserializing session data", e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Deserializing standard (uncompressed) session data");
                }
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {
                    return ois.readObject();
                } catch (ClassNotFoundException e) {
                    throw new SessionSerializerException("Class not found while deserializing standard session data", e);
                }
            }
        } catch (IOException e) {
            throw new SessionSerializerException("Error while decompressing and deserializing session data", e);
        }
    }

    /**
     * Checks if the byte array is GZIP compressed.
     *
     * @param compressed Byte array to check.
     * @return true if GZIP compressed, false otherwise.
     */
    private boolean isGzipped(byte[] compressed) {
        return (compressed.length >= 2 &&
                compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC) &&
                compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
