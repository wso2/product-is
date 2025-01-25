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

package org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword;

import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.ActionTestBase;

public class PreUpdatePasswordTestBase extends ActionTestBase {

    protected static final String PRE_UPDATE_PASSWORD_PATH = "/preUpdatePassword";

    protected static final String PRE_UPDATE_PASSWORD_ACTION_TYPE = "PRE_UPDATE_PASSWORD";

    protected static final String TEST_PROPERTIES_CERTIFICATE_ATTRIBUTE = "certificate";

    protected static final String TEST_CERTIFICATE = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR" +
            "zRENDQXBpZ0F3SUJBZ0lKQUs0eml2ckVsYzBJTUEwR0NTcUdTSWIzRFFFQkN3VUFNSUdETVJFd0R3WUQKVlFRRERBaE" +
            "NkV1JrYUdsdFlURUxNQWtHQTFVRUJoTUNVMHd4RURBT0JnTlZCQWdNQjFkbGMzUmxjbTR4RURBTwpCZ05WQkFjTUIwT" +
            "nZiRzl0WW04eERUQUxCZ05WQkFvTUJGZFRUekl4Q3pBSkJnTlZCQXNNQWxGQk1TRXdId1lKCktvWklodmNOQVFrQkZo" +
            "SmlkV1JrYUdsdFlYVkFkM052TWk1amIyMHdJQmNOTVRrd056RTJNRFF5TXpFd1doZ1AKTXpBeE9ERXhNVFl3TkRJek1" +
            "UQmFNSUdETVJFd0R3WURWUVFEREFoQ2RXUmthR2x0WVRFTE1Ba0dBMVVFQmhNQwpVMHd4RURBT0JnTlZCQWdNQjFkbG" +
            "MzUmxjbTR4RURBT0JnTlZCQWNNQjBOdmJHOXRZbTh4RFRBTEJnTlZCQW9NCkJGZFRUekl4Q3pBSkJnTlZCQXNNQWxGQ" +
            "k1TRXdId1lKS29aSWh2Y05BUWtCRmhKaWRXUmthR2x0WVhWQWQzTnYKTWk1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFF" +
            "QkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDcFo3V09VMTZpeGpiQwpiWGR3R3JhTW5xbmxnb2kzMDN5aVFxbHAySzlWTmZ" +
            "HT21nTlFhdFdlbjB0MVVWcjYxd0Y4eVlHaDJyc1lnbithCjhwYXVmUVVQQ1laeFRFR1FpT2RPZ0RNcE5tWW82ZHU2K2" +
            "MvenJqcHNncGh5SHIxNEZPVHAxaVRDSXBmanVwVjEKd1BUeXJveURySGRvMkpuOHI3V3F1cklJVTRBYllBN2NrdVVqL" +
            "0tqYUovTTZrZitwRFd5SVJvaDBKTFJlWWM4UQp5bmhYcjdrQWp5RnFqNitnWndBYkh4ckhrckVzYTJoVjQ0UFJXWjFQ" +
            "UERxTCswVU8veE1hQW5udndsdGd4QlVpCkhLUTFXWDVwdVVPaC9kQTQ5b0RsbEpraHpxd2d5eDQxc1FYbFNhVmdKakl" +
            "UZVdSQmdvNnh6ajNmd3VvenBGS1gKbzRaeXBITDNBZ01CQUFHakl6QWhNQjhHQTFVZEVRUVlNQmFDQkhkemJ6S0NDSG" +
            "R6YnpJdVkyOXRnZ1IzYzI4eQpNQTBHQ1NxR1NJYjNEUUVCQ3dVQUE0SUJBUUJTSzBKa1pyYlpvYmRDNHhZSG1IcnlVb" +
            "kZVbkZZWUFvZmc0TFVGCkJRbWxDY0NKR0ZwR1BtN2ZDWHM0Y0h4Z0hPVTN5SkhtQ2pYaU9FRTc2dzhIU0NRcVhkNmRO" +
            "SEwxRkxtN0pqQTUKTEZmbHhiWXNOcmVVNVpJTmREVGZvWmxSSXR0Mkd4MlpIa3pjQVRJZm1yUFNwODV2WDhGem1mbTN" +
            "BVTVpM3FXZQo4a2YyZk5nQjlMbE5XRFk1V09paVlHUWMrRk13WWdLcDJkNGM3dzMrWnRTUXJWRy9YdGpqYTJYV09Xdm" +
            "1sV3dLCnB4b3pyNjIvTTdUUmVkc3hJNU90bzJvWExGZXp1MUdCWHdpNEFaempMSFVsNWpSR2hMbkNZa05qdWZGZi9EQ" +
            "0cKeUFWdnpMVXQwZ2F0b0dJdTV2eG9la05JVWV5YTZpRzJBaG9jSmM0SEJMT3l4TXE3Ci0tLS0tRU5EIENFUlRJRklD" +
            "QVRFLS0tLS0K";

    protected static final String TEST_UPDATED_CERTIFICATE = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t" +
            "Ck1JSUQwRENDQXJpZ0F3SUJBZ0lCQVRBTkJna3Foa2lHOXcwQkFRVUZBREIvTVFzd0NRWURWUVFHRXdKR1VqRVQNCk1" +
            "CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURU9NQXdHQTFVRUJ3d0ZVR0Z5YVhNeERUQUxCZ05WQkFvTUJFUnANCmJXa3" +
            "hEVEFMQmdOVkJBc01CRTVUUWxVeEVEQU9CZ05WQkFNTUIwUnBiV2tnUTBFeEd6QVpCZ2txaGtpRzl3MEINCkNRRVdER" +
            "1JwYldsQVpHbHRhUzVtY2pBZUZ3MHhOREF4TWpneU1ETTJOVFZhRncweU5EQXhNall5TURNMk5UVmENCk1Gc3hDekFK" +
            "QmdOVkJBWVRBa1pTTVJNd0VRWURWUVFJREFwVGIyMWxMVk4wWVhSbE1TRXdId1lEVlFRS0RCaEoNCmJuUmxjbTVsZEN" +
            "CWGFXUm5hWFJ6SUZCMGVTQk1kR1F4RkRBU0JnTlZCQU1NQzNkM2R5NWthVzFwTG1aeU1JSUINCklqQU5CZ2txaGtpRz" +
            "l3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2cG5hUEtMSUtkdng5OEtXNjhsejhwR2ENClJSY1llcnNOR3FQanBpZ" +
            "k1WampFOEx1Q29YZ1BVMEhlUG5OVFVqcFNoQm55bktDdnJ0V2hOK2hhS2JTcCtRV1gNClN4aVRyVzk5SEJmQWwxTURR" +
            "eVdjdWtvRWI5Q3c2SU5jdFZVTjRpUnZrbjlUOEU2cTE3NFJiY253QS83eVRjN3ANCjFOQ3Z3KzZCL2FBTjlsMUcycFF" +
            "YZ1JkWUMvK0c2bzFJWkVIdFdocXpFOTduWTVRS051VVZEMFYwOWRjNUNEWUINCmFLanFldHd3djZERmsvR1JkT1NFZC" +
            "82YlcrMjB6MHFTSHBhM1lOVzZxU3AreDVweVltRHJ6UklSMDNvczZEYXUNClprQ2hTUnljL1dodnVyeDZvODVENnFwe" +
            "nl3bzh4d05hTFpIeFRRUGdjSUE1c3U5Wkl5dHY5TEgyRStsU3d3SUQNCkFRQUJvM3N3ZVRBSkJnTlZIUk1FQWpBQU1D" +
            "d0dDV0NHU0FHRytFSUJEUVFmRmgxUGNHVnVVMU5NSUVkbGJtVnkNCllYUmxaQ0JEWlhKMGFXWnBZMkYwWlRBZEJnTlZ" +
            "IUTRFRmdRVSt0dWdGdHlOK2NYZTF3eFVxZUE3WCt5UzNiZ3cNCkh3WURWUjBqQkJnd0ZvQVVoTXdxa2JCckdwODdIeG" +
            "Z2d2dQbmxHZ1ZSNjR3RFFZSktvWklodmNOQVFFRkJRQUQNCmdnRUJBSUVFbXFxaEV6ZVhaNENLaEU1VU05dkNLemtqN" +
            "Ul2OVRGcy9hOUNjUXVlcHpwbHQ3WVZtZXZCRk5PYzANCisxWnlSNHRYZ2k0KzVNSEd6aFlDSVZ2SG80aEtxWW0rSitv" +
            "NW13UUluZjFxb0FIdU83Q0xEM1dOYTFzS2NWVVYNCnZlcEl4Yy8xYUhackcrZFBlRUh0ME1kRmZPdzEzWWRVYzJGSDZ" +
            "BcUVkY0VMNGFWNVBYcTJlWVI4aFI0ektiYzENCmZCdHVxVXN2QThOV1NJeXpRMTZmeUd2ZStBTmY2dlh2VWl6eXZ3RH" +
            "JQUnYva2Z2TE5hM1pQbkxNTXhVOThNdmgNClBYeTNQa0I4Kys2VTRZM3ZkazJOaTJXWVlsSWxzOHlxYk00MzI3SUtta" +
            "0RjMlRpbVM4dTYwQ1Q0N21LVTdhRFkNCmNiVFY1UkRrcmxhWXdtNXlxbFRJZ2x2Q3Y3bz0NCi0tLS0tRU5EIENFUlRJ" +
            "RklDQVRFLS0tLS0K";

}
