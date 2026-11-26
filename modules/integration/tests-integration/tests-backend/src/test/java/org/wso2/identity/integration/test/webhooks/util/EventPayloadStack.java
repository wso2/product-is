/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.webhooks.util;

import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Utility class to manage a stack of expected event payloads for webhook tests.
 */
public class EventPayloadStack {

    private final LinkedList<Map.Entry<String, JSONObject>> eventPayloadStack = new LinkedList<>();

    /**
     * Adds an expected event payload for a specific event URI.
     * Multiple payloads can be stacked for the same event URI, when an operation is expected to trigger the same
     * event more than once (e.g. once in the sub organization and once in the root organization).
     *
     * @param eventUri        The event URI.
     * @param expectedPayload The expected event payload.
     */
    public synchronized void addExpectedPayload(String eventUri, JSONObject expectedPayload) {

        eventPayloadStack.add(new AbstractMap.SimpleImmutableEntry<>(eventUri, expectedPayload));
    }

    /**
     * Retrieves and removes the next expected payload in order.
     *
     * @return A Map.Entry containing the event URI and its expected payload.
     * @throws IllegalStateException If the stack is empty.
     */
    public synchronized Map.Entry<String, JSONObject> popExpectedPayload() {

        if (eventPayloadStack.isEmpty()) {
            throw new IllegalStateException("No more expected payloads in the stack.");
        }
        return eventPayloadStack.removeFirst();
    }

    /**
     * Checks if the stack is empty.
     *
     * @return True if the stack is empty, false otherwise.
     */
    public synchronized boolean isEmpty() {

        return eventPayloadStack.isEmpty();
    }

    /**
     * Clears all entries in the stack.
     */
    public synchronized void clearStack() {

        eventPayloadStack.clear();
    }
}
