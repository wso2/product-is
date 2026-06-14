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
        try (java.io.PushbackInputStream pbis = new java.io.PushbackInputStream(inputStream, 2)) {
            byte[] header = new byte[2];
            int len = pbis.read(header);
            if (len <= 0) {
                return null;
            }
            pbis.unread(header, 0, len);

            boolean isCompressed = isGzipped(header, len);
            if (log.isDebugEnabled()) {
                if (isCompressed) {
                    log.debug("Deserializing GZIP compressed session data");
                } else {
                    log.debug("Deserializing standard (uncompressed) session data");
                }
            }

            java.io.InputStream source = isCompressed ? new GZIPInputStream(pbis) : pbis;
            return readSessionObject(source);
        } catch (IOException e) {
            throw new SessionSerializerException("Error while decompressing and deserializing session data", e);
        }
    }

    private Object readSessionObject(java.io.InputStream source) throws SessionSerializerException {
        try (ObjectInputStream ois = new ObjectInputStream(source)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SessionSerializerException("Error while deserializing session data", e);
        }
    }

    /**
     * Checks if the byte array is GZIP compressed.
     *
     * @param header Byte array header to check.
     * @param len Length of read bytes.
     * @return true if GZIP compressed, false otherwise.
     */
    private boolean isGzipped(byte[] header, int len) {
        return (len >= 2 &&
                header[0] == (byte) (GZIPInputStream.GZIP_MAGIC) &&
                header[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
