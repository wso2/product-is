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

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookRequest;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookRequestEventProfile;
import org.wso2.identity.integration.test.rest.api.server.webhook.management.v1.model.WebhookResponse;
import org.wso2.identity.integration.test.restclients.WebhooksRestClient;
import org.wso2.identity.integration.test.webhooks.mockservice.WebhookMockService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Manager class for handling and verifying webhook events in tests.
 */
public class WebhookEventTestManager {

    private static final int START_PORT = 8580;
    private static final int PORT_LIMIT = 8590;
    private static final int MAX_RETRIES_ON_EVENTS_RECEIVED = 3;
    private static final int WAIT_TIME_IN_MILLIS_ON_EVENTS_RECEIVED = 500;
    private static final String SERVER_BASE_URL = "https://localhost:9853/";
    private static final String WSO2_EVENT_PROFILE_URI = "https://schemas.identity.wso2.org/events";
    private static final AtomicInteger currentPort = new AtomicInteger(START_PORT);

    private final String webhookEndpointPath;
    private final String eventProfile;
    private final List<String> channelsSubscribed;
    private final String testName;
    private final WebhooksRestClient webhooksRestClient;
    private final EventPayloadStack eventPayloadStack;
    private WebhookMockService mockService;
    private String webhookEndpoint;
    private final String webhookId;

    private static final Logger LOG = LoggerFactory.getLogger(WebhookEventTestManager.class);

    /**
     * Constructor to initialize the WebhookEventTestManager.
     *
     * @param webhookEndpointPath the path for the webhook endpoint.
     * @param eventProfile        the event profile to be used for the webhook. Typically, "WSO2" for WSO2 events.
     * @param channelsSubscribed  the list of channels to which the webhook is subscribed.
     * @param testName            the name of the test case for which the webhook is being set up.
     * @param automationContext   the automation context containing tenant information and other configurations.
     * @throws Exception if an error occurs during initialization, such as starting the mock server or creating the webhook.
     */
    public WebhookEventTestManager(String webhookEndpointPath, String eventProfile, List<String> channelsSubscribed,
                                   String testName, AutomationContext automationContext) throws Exception {

        this.webhookEndpointPath = webhookEndpointPath;
        this.eventProfile = eventProfile;
        this.channelsSubscribed = channelsSubscribed;
        this.testName = testName;
        this.eventPayloadStack = new EventPayloadStack();

        startMockServer();
        this.webhooksRestClient = new WebhooksRestClient(SERVER_BASE_URL, automationContext.getContextTenant());
        this.webhookId = createWebhook();
    }

    /**
     * Teardown method to clean up resources after tests.
     *
     * @throws Exception if an error occurs during teardown.
     */
    public void teardown() throws Exception {

        stopMockServer();
        deleteWebhook();
        eventPayloadStack.clearStack();
    }

    /**
     * Stacks an expected payload for a specific event URI.
     *
     * @param eventUri        the URI of the event for which the payload is expected.
     * @param expectedPayload the expected JSON payload for the event.
     */
    public void stackExpectedEventPayload(String eventUri, JSONObject expectedPayload) {

        LOG.info("Stacking expected payload for event URI: {}", eventUri);

        eventPayloadStack.addExpectedPayload(eventUri, expectedPayload);
    }

    /**
     * Validates the event payloads received by the webhook against the expected payloads stacked earlier.
     *
     * @throws Exception if an error occurs during validation, such as if no payloads are received or if validation fails.
     */
    public void validateStackedEventPayloads() throws Exception {

        if (eventPayloadStack.isEmpty()) {
            return;
        }

        waitForEvents();

        try {
            while (!eventPayloadStack.isEmpty()) {
                Map.Entry<String, JSONObject> expectedEntry = eventPayloadStack.popExpectedPayload();
                validateEventPayloadForEventUri(expectedEntry.getKey(), expectedEntry.getValue());
            }

            if (!mockService.getOrderedRequests().isEmpty()) {
                throw new AssertionError(
                        "There are " + mockService.getOrderedRequests().size() +
                                " unexpected event notifications received to the mock service.");
            }
        } finally {
            mockService.clearOrderedRequests();
            eventPayloadStack.clearStack();
        }
    }

    private void waitForEvents() throws InterruptedException {

        int retryCount = 0;

        while (mockService.getOrderedRequests().isEmpty() && retryCount < MAX_RETRIES_ON_EVENTS_RECEIVED) {
            LOG.info("No events received for the webhook. Retrying... Attempt: {}/{}", retryCount + 1,
                    MAX_RETRIES_ON_EVENTS_RECEIVED);
            TimeUnit.MILLISECONDS.sleep(WAIT_TIME_IN_MILLIS_ON_EVENTS_RECEIVED);
            retryCount++;
        }

        if (mockService.getOrderedRequests().isEmpty()) {
            throw new AssertionError(
                    "No events received for the webhook after " + MAX_RETRIES_ON_EVENTS_RECEIVED + " retries.");
        }
    }

    private void validateEventPayloadForEventUri(String eventUri, JSONObject expectedEventsObjectInPayload)
            throws Exception {

        waitForSpecificEvent(eventUri);

        List<JSONObject> receivedPayloads = extractReceivedPayloads();
        boolean isMatched = matchAndValidatePayload(eventUri, expectedEventsObjectInPayload, receivedPayloads);

        if (!isMatched) {
            throw new AssertionError("No matching payload found for event URI: " + eventUri);
        }
    }

    private void waitForSpecificEvent(String eventUri) throws InterruptedException {

        int retryCount = 0;

        while (retryCount < MAX_RETRIES_ON_EVENTS_RECEIVED) {
            List<JSONObject> receivedPayloads = extractReceivedPayloads();

            boolean eventExists = receivedPayloads.stream()
                    .anyMatch(payload -> {
                        try {
                            return payload != null && payload.has("events") &&
                                    payload.getJSONObject("events").has(eventUri);
                        } catch (JSONException e) {
                            LOG.error("Error parsing JSON payload for event URI '{}': {}", eventUri, e.getMessage());
                            return false;
                        }
                    });

            if (eventExists) {
                return;
            }

            LOG.info("Event for URI '{}' not found in received payloads. Retrying... Attempt: {}/{}",
                    eventUri, retryCount + 1, MAX_RETRIES_ON_EVENTS_RECEIVED);
            TimeUnit.MILLISECONDS.sleep(WAIT_TIME_IN_MILLIS_ON_EVENTS_RECEIVED);
            retryCount++;
        }

        throw new AssertionError("Event for URI '" + eventUri + "' not found after " +
                MAX_RETRIES_ON_EVENTS_RECEIVED + " retries.");
    }

    private List<JSONObject> extractReceivedPayloads() {

        return mockService.getOrderedRequests()
                .stream()
                .map(request -> {
                    try {
                        return new JSONObject(request.getBodyAsString());
                    } catch (JSONException e) {
                        LOG.error("Invalid JSON payload: {}", e.getMessage());
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean matchAndValidatePayload(String eventUri, JSONObject expectedEventsObject,
                                            List<JSONObject> receivedPayloads) throws Exception {

        for (int i = 0; i < receivedPayloads.size(); i++) {
            JSONObject receivedPayload = receivedPayloads.get(i);

            if (receivedPayload != null && receivedPayload.has("events") &&
                    receivedPayload.getJSONObject("events").has(eventUri)) {

                try {
                    EventPayloadValidator.validateCommonEventPayloadFields(eventUri, receivedPayload);
                    JSONObject actualEventsObject =
                            EventPayloadValidator.extractEventPayload(eventUri, receivedPayload);

                    LOG.info("Validating received event payload: {} against expected payload: {} for event URI: {}.",
                            actualEventsObject, expectedEventsObject, eventUri);

                    EventPayloadValidator.validateEventField(actualEventsObject, expectedEventsObject);
                    LOG.info("Payload validation successful for event URI: {}", eventUri);
                    LOG.info("Removing matched payload from request list of the mock service for event URI: {}",
                            eventUri);
                    receivedPayloads.remove(i);
                    mockService.removeRequest(mockService.getOrderedRequests().get(i));
                    return true;

                } catch (IllegalArgumentException e) {
                    throw new AssertionError(
                            "Payload validation failed for event URI: " + eventUri + ". Error: " + e.getMessage(), e);
                }
            }
        }
        return false;
    }

    private void startMockServer() throws Exception {

        int port = getNextAvailablePort();
        mockService = new WebhookMockService();

        try {
            mockService.startServer(port);
            webhookEndpoint = mockService.registerWebhookEndpoint(webhookEndpointPath);

            LOG.info("Webhook mock server started on port: {}", port);
            LOG.info("Webhook endpoint registered at: {}", webhookEndpoint);

            Thread.sleep(500); // Ensure server is fully initialized
        } catch (Exception e) {
            LOG.error("Failed to start the mock server on port: {}", port, e);
            throw e;
        }
    }

    private void stopMockServer() {

        if (mockService != null) {
            mockService.stopServer();
        }
    }

    private int getNextAvailablePort() {

        int port = currentPort.getAndIncrement();
        if (port > PORT_LIMIT) {
            currentPort.set(START_PORT);
            port = currentPort.getAndIncrement();
        }
        return port;
    }

    private String createWebhook() throws Exception {

        WebhookRequest webhookRequest = new WebhookRequest();
        webhookRequest.name(testName)
                .endpoint(webhookEndpoint)
                .secret("secretKey")
                .status(WebhookRequest.StatusEnum.ACTIVE)
                .eventProfile(new WebhookRequestEventProfile().name(eventProfile)
                        .uri(getEventProfileURI()));

        for (String channel : channelsSubscribed) {
            webhookRequest.addChannelsSubscribedItem(channel);
        }

        WebhookResponse response = webhooksRestClient.createWebhook(webhookRequest);
        return response.getId();
    }

    private void deleteWebhook() throws Exception {

        if (webhookId != null) {
            webhooksRestClient.deleteWebhook(webhookId);
        }
    }

    private String getEventProfileURI() {

        return "WSO2".equals(eventProfile) ? WSO2_EVENT_PROFILE_URI : null;
    }
}
