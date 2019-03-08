# IAM Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Authenticate Super Tenant User | Select random super tenant users and authenticate through the RemoteUserStoreManagerService. |
| SAML2 SSO Redirect Binding | Obtain a SAML 2 assertion response using redirect binding. |
| Auth Code Grant Redirect With Consent | Obtain an access token using the OAuth 2.0 authorization code grant type. |
| Implicit Grant Redirect With Consent | Obtain an access token using the OAuth 2.0 implicit grant type. |
| Password Grant Type | Obtain an access token using the OAuth 2.0 password grant type. |
| Client Credentials Grant Type | Obtain an access token using the OAuth 2.0 client credential grant type. |
| OIDC Auth Code Grant Redirect With Consent | Obtain an access token and an id token using the OAuth 2.0 authorization code grant type. |
| OIDC Implicit Grant Redirect With Consent | Obtain an access token and an id token using the OAuth 2.0 implicit grant type. |
| OIDC Password Grant Type | Obtain an access token and an id token using the OAuth 2.0 password grant type. |
| OIDC Auth Code Request Path Authenticator With Consent | Obtain an access token and an id token using the request path authenticator. |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time and split the test results into warm-up and measurement parts and use the measurement part to compute the
performance metrics. For this particular instance, the duration of each test is **15 minutes** and the warm-up period is **5 minutes**.

We run the performance tests under different numbers of concurrent users and heap sizes to gain a better understanding on how the server reacts to different loads.

The main performance metrics:

1. **Throughput**: The number of requests that the WSO2 Identity Server processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for a given operation of the WSO2 Identity Server. The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 2G |
| Concurrent Users | The number of users accessing the application at the same time. | 50, 100, 150, 300, 500 |
| IS Instance Type | The AWS instance type used to run the Identity Server. | [**c5.xlarge**](https://aws.amazon.com/ec2/instance-types/) |

The following are the measurements collected from each performance test conducted for a given combination of
test parameters.

| Measurement | Description |
| --- | --- |
| Error % | Percentage of requests with errors |
| Average Response Time (ms) | The average response time of a set of results |
| Standard Deviation of Response Time (ms) | The Standard Deviation of the response time. |
| 99th Percentile of Response Time (ms) | 99% of the requests took no more than this time. The remaining samples took at least as long as this |
| Throughput (Requests/sec) | The throughput measured in requests per second. |
| Average Memory Footprint After Full GC (M) | The average memory consumed by the application after a full garbage collection event. |

The following is the summary of performance test results collected for the measurement period.

|  Scenario Name | Concurrent Users | Label | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | WSO2 Identity Server GC Throughput (%) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
|  Authenticate Super Tenant User | 50 | Authenticate | 0 | 1928.97 | 25.74 | 6.82 | 46 | 98.75 |
|  Authenticate Super Tenant User | 100 | Authenticate | 0 | 1958.26 | 50.88 | 18.44 | 104 | 98.58 |
|  Authenticate Super Tenant User | 150 | Authenticate | 0 | 1943.09 | 76.99 | 31.38 | 169 | 98.49 |
|  Authenticate Super Tenant User | 300 | Authenticate | 0 | 1875.01 | 159.78 | 57.59 | 327 | 98.09 |
|  Authenticate Super Tenant User | 500 | Authenticate | 0 | 1360.77 | 367.28 | 355.78 | 1847 | 97.52 |
|  Auth Code Grant Redirect With Consent | 50 | Common Auth Login HTTP Request | 0 | 20.18 | 246.4 | 173.93 | 655 | 99.46 |
|  Auth Code Grant Redirect With Consent | 50 | Common Auth Login HTTP Request Redirect | 0 | 20.18 | 279.32 | 211.06 | 867 | 99.46 |
|  Auth Code Grant Redirect With Consent | 50 | Get Authorization Code | 0 | 20.18 | 358.29 | 255.48 | 943 | 99.46 |
|  Auth Code Grant Redirect With Consent | 50 | Get access token | 0 | 20.17 | 1256.44 | 837.91 | 2735 | 99.46 |
|  Auth Code Grant Redirect With Consent | 50 | Send request to authorize end poiont | 0 | 20.18 | 337.48 | 240.43 | 907 | 99.46 |
|  Auth Code Grant Redirect With Consent | 100 | Common Auth Login HTTP Request | 0 | 20.44 | 476.23 | 366.84 | 1471 | 99.51 |
|  Auth Code Grant Redirect With Consent | 100 | Common Auth Login HTTP Request Redirect | 0 | 20.43 | 620.83 | 499.46 | 2191 | 99.51 |
|  Auth Code Grant Redirect With Consent | 100 | Get Authorization Code | 0 | 20.4 | 707.04 | 533.34 | 2159 | 99.51 |
|  Auth Code Grant Redirect With Consent | 100 | Get access token | 0 | 20.4 | 2372.68 | 1592.76 | 5503 | 99.51 |
|  Auth Code Grant Redirect With Consent | 100 | Send request to authorize end poiont | 0 | 20.46 | 709.6 | 537.35 | 2207 | 99.51 |
|  Auth Code Grant Redirect With Consent | 150 | Common Auth Login HTTP Request | 0 | 19.59 | 747.71 | 601.29 | 2575 | 99.41 |
|  Auth Code Grant Redirect With Consent | 150 | Common Auth Login HTTP Request Redirect | 0 | 19.53 | 1044.99 | 830.21 | 3631 | 99.41 |
|  Auth Code Grant Redirect With Consent | 150 | Get Authorization Code | 0 | 19.54 | 1132.92 | 859.14 | 3487 | 99.41 |
|  Auth Code Grant Redirect With Consent | 150 | Get access token | 0 | 19.51 | 3536.69 | 2329.19 | 8319 | 99.41 |
|  Auth Code Grant Redirect With Consent | 150 | Send request to authorize end poiont | 0 | 19.55 | 1181.06 | 905.16 | 3711 | 99.41 |
|  Auth Code Grant Redirect With Consent | 300 | Common Auth Login HTTP Request | 0 | 20.9 | 1446.5 | 1223.47 | 5023 | 99.38 |
|  Auth Code Grant Redirect With Consent | 300 | Common Auth Login HTTP Request Redirect | 0 | 20.84 | 1928.51 | 1580.46 | 6463 | 99.38 |
|  Auth Code Grant Redirect With Consent | 300 | Get Authorization Code | 0 | 20.97 | 2218.67 | 1706.5 | 6623 | 99.38 |
|  Auth Code Grant Redirect With Consent | 300 | Get access token | 0 | 20.82 | 6239.19 | 4171.88 | 14271 | 99.38 |
|  Auth Code Grant Redirect With Consent | 300 | Send request to authorize end poiont | 0 | 20.75 | 2444.47 | 2011.53 | 8159 | 99.38 |
|  Auth Code Grant Redirect With Consent | 500 | Common Auth Login HTTP Request | 0 | 20.64 | 1992.16 | 1446.64 | 5439 | 99.33 |
|  Auth Code Grant Redirect With Consent | 500 | Common Auth Login HTTP Request Redirect | 0 | 20.56 | 2869.23 | 2085.78 | 7743 | 99.33 |
|  Auth Code Grant Redirect With Consent | 500 | Get Authorization Code | 0 | 20.49 | 3540.63 | 2452.74 | 8255 | 99.33 |
|  Auth Code Grant Redirect With Consent | 500 | Get access token | 0 | 20.76 | 11800 | 7551.63 | 22911 | 99.33 |
|  Auth Code Grant Redirect With Consent | 500 | Send request to authorize end poiont | 0 | 20.83 | 3664.03 | 2628.19 | 9535 | 99.33 |
|  Client Credentials Grant Type | 50 | Get Token Client Credential Grant | 0 | 2202.66 | 22.54 | 11.34 | 53 | 99.22 |
|  Client Credentials Grant Type | 100 | Get Token Client Credential Grant | 0 | 2369.08 | 42.04 | 47.93 | 100 | 99.11 |
|  Client Credentials Grant Type | 150 | Get Token Client Credential Grant | 0 | 2378.18 | 62.9 | 76.94 | 161 | 99.03 |
|  Client Credentials Grant Type | 300 | Get Token Client Credential Grant | 0 | 2225.71 | 134.59 | 128.78 | 499 | 98.82 |
|  Client Credentials Grant Type | 500 | Get Token Client Credential Grant | 0.04 | 1904.25 | 262.45 | 2023.71 | 671 | 98.39 |
|  Implicit Grant Redirect With Consent | 50 | Common Auth Login HTTP Request | 0 | 40.61 | 289.42 | 199.06 | 703 | 99.44 |
|  Implicit Grant Redirect With Consent | 50 | Common Auth Login HTTP Request Redirect | 0 | 40.61 | 302.18 | 217.87 | 831 | 99.44 |
|  Implicit Grant Redirect With Consent | 50 | Get Access token | 0 | 40.61 | 252.07 | 183.79 | 711 | 99.44 |
|  Implicit Grant Redirect With Consent | 50 | Send request to authorize end point | 0 | 40.62 | 387.51 | 267.95 | 963 | 99.44 |
|  Implicit Grant Redirect With Consent | 100 | Common Auth Login HTTP Request | 0 | 39.66 | 538.31 | 402.29 | 1559 | 99.42 |
|  Implicit Grant Redirect With Consent | 100 | Common Auth Login HTTP Request Redirect | 0 | 39.65 | 655.22 | 499.78 | 2047 | 99.42 |
|  Implicit Grant Redirect With Consent | 100 | Get Access token | 0 | 39.64 | 527.34 | 430.35 | 1871 | 99.42 |
|  Implicit Grant Redirect With Consent | 100 | Send request to authorize end point | 0 | 39.66 | 797.03 | 586.13 | 2255 | 99.42 |
|  Implicit Grant Redirect With Consent | 150 | Common Auth Login HTTP Request | 0 | 40.38 | 742.38 | 594.13 | 2543 | 99.34 |
|  Implicit Grant Redirect With Consent | 150 | Common Auth Login HTTP Request Redirect | 0 | 40.37 | 1006.97 | 833.7 | 3647 | 99.34 |
|  Implicit Grant Redirect With Consent | 150 | Get Access token | 0 | 40.35 | 674.52 | 532.94 | 2319 | 99.34 |
|  Implicit Grant Redirect With Consent | 150 | Send request to authorize end point | 0 | 40.39 | 1283.59 | 1046.77 | 4447 | 99.34 |
|  Implicit Grant Redirect With Consent | 300 | Common Auth Login HTTP Request | 0 | 38.86 | 1433.88 | 1222.88 | 5471 | 99.23 |
|  Implicit Grant Redirect With Consent | 300 | Common Auth Login HTTP Request Redirect | 0 | 38.88 | 2119.09 | 1867.19 | 8255 | 99.23 |
|  Implicit Grant Redirect With Consent | 300 | Get Access token | 0 | 38.88 | 1261.72 | 1056.95 | 4831 | 99.23 |
|  Implicit Grant Redirect With Consent | 300 | Send request to authorize end point | 0 | 38.8 | 2884.98 | 2443.98 | 11391 | 99.23 |
|  Implicit Grant Redirect With Consent | 500 | Common Auth Login HTTP Request | 0 | 40.81 | 2164.79 | 1637.04 | 6303 | 99.21 |
|  Implicit Grant Redirect With Consent | 500 | Common Auth Login HTTP Request Redirect | 0 | 40.81 | 3390.53 | 2721.35 | 10879 | 99.21 |
|  Implicit Grant Redirect With Consent | 500 | Get Access token | 0 | 40.8 | 2256.48 | 1646.74 | 7103 | 99.21 |
|  Implicit Grant Redirect With Consent | 500 | Send request to authorize end point | 0 | 40.79 | 4363.86 | 3581.89 | 14463 | 99.21 |
|  Password Grant Type | 50 | GetToken_Password_Grant | 0 | 152.32 | 328.3 | 245.77 | 931 | 99.57 |
|  Password Grant Type | 100 | GetToken_Password_Grant | 0 | 147.91 | 675.14 | 546.05 | 2351 | 99.55 |
|  Password Grant Type | 150 | GetToken_Password_Grant | 0 | 148.12 | 1010.62 | 850.48 | 3711 | 99.48 |
|  Password Grant Type | 300 | GetToken_Password_Grant | 0 | 148.53 | 2012.98 | 1534.8 | 6271 | 99.38 |
|  Password Grant Type | 500 | GetToken_Password_Grant | 0 | 153.42 | 3243.27 | 2309.17 | 8767 | 99.4 |
|  OIDC Auth Code Grant Redirect With Consent | 50 | Common Auth Login HTTP Request | 0 | 21.06 | 232.71 | 168.44 | 631 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 50 | Common Auth Login HTTP Request Redirect | 0 | 21.06 | 268.83 | 203.08 | 839 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 50 | Get Authorization Code | 0 | 21.05 | 339.05 | 243.15 | 895 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 50 | Get tokens | 0 | 21.05 | 1212.25 | 811.25 | 2607 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 50 | Send request to authorize end poiont | 0 | 21.07 | 322 | 230.4 | 875 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 100 | Common Auth Login HTTP Request | 0 | 20.7 | 465.05 | 363.31 | 1455 | 99.46 |
|  OIDC Auth Code Grant Redirect With Consent | 100 | Common Auth Login HTTP Request Redirect | 0 | 20.69 | 611.25 | 482.01 | 2111 | 99.46 |
|  OIDC Auth Code Grant Redirect With Consent | 100 | Get Authorization Code | 0 | 20.69 | 699.3 | 534.68 | 2159 | 99.46 |
|  OIDC Auth Code Grant Redirect With Consent | 100 | Get tokens | 0 | 20.67 | 2361.55 | 1603.74 | 5471 | 99.46 |
|  OIDC Auth Code Grant Redirect With Consent | 100 | Send request to authorize end poiont | 0 | 20.67 | 691.45 | 521.8 | 2111 | 99.46 |
|  OIDC Auth Code Grant Redirect With Consent | 150 | Common Auth Login HTTP Request | 0 | 19.97 | 749.07 | 593.66 | 2447 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 150 | Common Auth Login HTTP Request Redirect | 0 | 19.97 | 1008.23 | 793.69 | 3439 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 150 | Get Authorization Code | 0 | 19.94 | 1125.97 | 850.78 | 3471 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 150 | Get tokens | 0 | 19.9 | 3470.53 | 2251.99 | 8159 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 150 | Send request to authorize end poiont | 0 | 19.96 | 1151.5 | 871.26 | 3551 | 99.47 |
|  OIDC Auth Code Grant Redirect With Consent | 300 | Common Auth Login HTTP Request | 0 | 20.24 | 1520.86 | 1244.32 | 4991 | 99.36 |
|  OIDC Auth Code Grant Redirect With Consent | 300 | Common Auth Login HTTP Request Redirect | 0 | 20.23 | 1983.76 | 1554.92 | 6271 | 99.36 |
|  OIDC Auth Code Grant Redirect With Consent | 300 | Get Authorization Code | 0 | 20.16 | 2268.28 | 1685.25 | 6367 | 99.36 |
|  OIDC Auth Code Grant Redirect With Consent | 300 | Get tokens | 0 | 20.25 | 6473.64 | 4222.29 | 14783 | 99.36 |
|  OIDC Auth Code Grant Redirect With Consent | 300 | Send request to authorize end poiont | 0 | 20.34 | 2480.82 | 1896.31 | 7199 | 99.36 |
|  OIDC Auth Code Grant Redirect With Consent | 500 | Common Auth Login HTTP Request | 0 | 21.09 | 2005.61 | 1450.86 | 5311 | 99.29 |
|  OIDC Auth Code Grant Redirect With Consent | 500 | Common Auth Login HTTP Request Redirect | 0 | 21.02 | 2864.71 | 2077.96 | 7551 | 99.29 |
|  OIDC Auth Code Grant Redirect With Consent | 500 | Get Authorization Code | 0 | 21.02 | 3538.8 | 2467.69 | 8319 | 99.29 |
|  OIDC Auth Code Grant Redirect With Consent | 500 | Get tokens | 0 | 20.93 | 11615.82 | 7638.61 | 23167 | 99.29 |
|  OIDC Auth Code Grant Redirect With Consent | 500 | Send request to authorize end poiont | 0 | 21.07 | 3494.92 | 2506.8 | 8831 | 99.29 |
|  OIDC Implicit Grant Redirect With Consent | 50 | Common Auth Login HTTP Request | 0 | 26.84 | 270.2 | 194.93 | 735 | 99.48 |
|  OIDC Implicit Grant Redirect With Consent | 50 | Common Auth Login HTTP Request Redirect | 0 | 26.84 | 320.25 | 242.39 | 987 | 99.48 |
|  OIDC Implicit Grant Redirect With Consent | 50 | Get tokens | 0 | 26.83 | 887.17 | 607.33 | 2143 | 99.48 |
|  OIDC Implicit Grant Redirect With Consent | 50 | Send request to authorize end point | 0 | 26.82 | 384.35 | 278.82 | 1063 | 99.48 |
|  OIDC Implicit Grant Redirect With Consent | 100 | Common Auth Login HTTP Request | 0 | 26.54 | 532 | 411.49 | 1655 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 100 | Common Auth Login HTTP Request Redirect | 0 | 26.55 | 682.78 | 549.88 | 2367 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 100 | Get tokens | 0 | 26.53 | 1755.96 | 1221.59 | 4575 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 100 | Send request to authorize end point | 0 | 26.52 | 793.41 | 595.85 | 2351 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 150 | Common Auth Login HTTP Request | 0 | 26.64 | 797.2 | 632.68 | 2543 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 150 | Common Auth Login HTTP Request Redirect | 0 | 26.64 | 1076.9 | 857.38 | 3599 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 150 | Get tokens | 0 | 26.64 | 2498.64 | 1712.74 | 6399 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 150 | Send request to authorize end point | 0 | 26.65 | 1243.58 | 947.63 | 3695 | 99.41 |
|  OIDC Implicit Grant Redirect With Consent | 300 | Common Auth Login HTTP Request | 0 | 27.23 | 1594.42 | 1256.96 | 4959 | 99.33 |
|  OIDC Implicit Grant Redirect With Consent | 300 | Common Auth Login HTTP Request Redirect | 0 | 27.14 | 2246.13 | 1734.86 | 6815 | 99.33 |
|  OIDC Implicit Grant Redirect With Consent | 300 | Get tokens | 0 | 27.21 | 4395.53 | 2856.19 | 10623 | 99.33 |
|  OIDC Implicit Grant Redirect With Consent | 300 | Send request to authorize end point | 0 | 27.17 | 2729.63 | 2074.7 | 8031 | 99.33 |
|  OIDC Implicit Grant Redirect With Consent | 500 | Common Auth Login HTTP Request | 0 | 27.04 | 2347.34 | 1666.84 | 6207 | 99.29 |
|  OIDC Implicit Grant Redirect With Consent | 500 | Common Auth Login HTTP Request Redirect | 0 | 26.97 | 3576.09 | 2535.74 | 9343 | 99.29 |
|  OIDC Implicit Grant Redirect With Consent | 500 | Get tokens | 0 | 26.83 | 8202.44 | 5232.18 | 17407 | 99.29 |
|  OIDC Implicit Grant Redirect With Consent | 500 | Send request to authorize end point | 0 | 26.97 | 4257.96 | 3027.38 | 10623 | 99.29 |
|  OIDC Password Grant Type | 50 | GetToken_Password_Grant | 0 | 42.39 | 1177.69 | 779.81 | 2767 | 99.63 |
|  OIDC Password Grant Type | 100 | GetToken_Password_Grant | 0 | 41.98 | 2370.5 | 1645.1 | 6207 | 99.61 |
|  OIDC Password Grant Type | 150 | GetToken_Password_Grant | 0 | 41.16 | 3620.55 | 2575.73 | 9983 | 99.57 |
|  OIDC Password Grant Type | 300 | GetToken_Password_Grant | 0 | 41.09 | 7260.3 | 4965.96 | 18559 | 99.5 |
|  OIDC Password Grant Type | 500 | GetToken_Password_Grant | 0 | 40.43 | 12108.83 | 7745.01 | 26111 | 99.51 |
|  OIDC Auth Code Request Path Authenticator With Consent | 50 | Get Authorization Code | 0 | 20.64 | 344.5 | 246.5 | 983 | 99.52 |
|  OIDC Auth Code Request Path Authenticator With Consent | 50 | Get tokens | 0 | 20.64 | 1245.56 | 808.62 | 2719 | 99.52 |
|  OIDC Auth Code Request Path Authenticator With Consent | 50 | Send request to authorize end poiont | 0 | 20.63 | 829.54 | 548.02 | 1967 | 99.52 |
|  OIDC Auth Code Request Path Authenticator With Consent | 100 | Get Authorization Code | 0 | 20.46 | 709.5 | 536.6 | 2207 | 99.49 |
|  OIDC Auth Code Request Path Authenticator With Consent | 100 | Get tokens | 0 | 20.45 | 2427.56 | 1591.49 | 5567 | 99.49 |
|  OIDC Auth Code Request Path Authenticator With Consent | 100 | Send request to authorize end poiont | 0 | 20.48 | 1736.21 | 1168.79 | 4319 | 99.49 |
|  OIDC Auth Code Request Path Authenticator With Consent | 150 | Get Authorization Code | 0 | 20.67 | 1085.26 | 834.15 | 3327 | 99.49 |
|  OIDC Auth Code Request Path Authenticator With Consent | 150 | Get tokens | 0 | 20.62 | 3509.49 | 2321.14 | 8319 | 99.49 |
|  OIDC Auth Code Request Path Authenticator With Consent | 150 | Send request to authorize end poiont | 0 | 20.65 | 2623.96 | 1812.81 | 6495 | 99.49 |
|  OIDC Auth Code Request Path Authenticator With Consent | 300 | Get Authorization Code | 0 | 20.87 | 2291.83 | 1731.23 | 6623 | 99.39 |
|  OIDC Auth Code Request Path Authenticator With Consent | 300 | Get tokens | 0 | 20.86 | 6593.44 | 4328.89 | 14847 | 99.39 |
|  OIDC Auth Code Request Path Authenticator With Consent | 300 | Send request to authorize end poiont | 0 | 20.98 | 5439.43 | 3694.9 | 12607 | 99.39 |
|  OIDC Auth Code Request Path Authenticator With Consent | 500 | Get Authorization Code | 0 | 21.61 | 3482.17 | 2410.82 | 8383 | 99.3 |
|  OIDC Auth Code Request Path Authenticator With Consent | 500 | Get tokens | 0 | 21.42 | 11259 | 7319.37 | 22783 | 99.3 |
|  OIDC Auth Code Request Path Authenticator With Consent | 500 | Send request to authorize end poiont | 0 | 21.49 | 8131.44 | 5447.53 | 17407 | 99.3 |
|  SAML2 SSO Redirect Binding | 50 | Identity Provider Login | 0 | 56.63 | 586.71 | 366.29 | 1271 | 99.01 |
|  SAML2 SSO Redirect Binding | 50 | Initial SAML Request | 0 | 56.63 | 294.93 | 203.11 | 787 | 99.01 |
|  SAML2 SSO Redirect Binding | 100 | Identity Provider Login | 0 | 57.25 | 1110.2 | 731.82 | 2655 | 99.03 |
|  SAML2 SSO Redirect Binding | 100 | Initial SAML Request | 0 | 57.24 | 633.47 | 475.83 | 1927 | 99.03 |
|  SAML2 SSO Redirect Binding | 150 | Identity Provider Login | 0 | 58.31 | 1610.22 | 1056.69 | 3887 | 98.96 |
|  SAML2 SSO Redirect Binding | 150 | Initial SAML Request | 0 | 58.29 | 957.28 | 722.13 | 2847 | 98.96 |
|  SAML2 SSO Redirect Binding | 300 | Identity Provider Login | 0 | 56.45 | 3252.38 | 2693.41 | 8959 | 98.81 |
|  SAML2 SSO Redirect Binding | 300 | Initial SAML Request | 0 | 56.52 | 2027.63 | 1634.77 | 6751 | 98.81 |
|  SAML2 SSO Redirect Binding | 500 | Identity Provider Login | 0 | 58 | 5293.73 | 3652.3 | 13439 | 98.8 |
|  SAML2 SSO Redirect Binding | 500 | Initial SAML Request | 0 | 58.04 | 3252.56 | 2493.36 | 10559 | 98.8 |
