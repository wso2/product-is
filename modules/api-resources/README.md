
Aggregates the API implementations from [identity-api-user](https://github.com/wso2/identity-api-user/), 
[identity-api-server](https://github.com/wso2/identity-api-server/) and other APIs builds a single webapp inorder 
to expose the multiple API endpoints in WSO2 Identity Server depending on the profile.

Current profiles,
1. full (api-respources-full.war)


#### Exposing a new API in specific profile

1. Add the dependency of the API implementation into the dependencyManagement section of api-resources pom.xml
2. Include the dependency of the API implementation into the dependencies section of corresponding profile module pom file
3. Open the corresponding profile module beans.xml (<profile-module>/src/main/webapp/WEB-INF/beans.xml)

    ```
     <profile-module>
     ├── pom.xml
     ├── src
     │   └── main
     │       └── webapp
     │           ├── META-INF
     |           |   ├── webapp-classloading.xml
     │           └── WEB-INF
     │               ├── beans.xml
     │               └── web.xml
    ```
   
4. Import the API CXF xml file. 

    ```
       <import resource="classpath:META-INF/cxf/workflow-engine-server-v1-cxf.xml"/>
       <import resource="classpath:META-INF/cxf/claim-management-server-v1-cxf.xml"/>
       <import resource="classpath:META-INF/cxf/challenge-server-v1-cxf.xml"/>
       <import resource="classpath:META-INF/cxf/email-template-server-v1-cxf.xml"/>
    ```

5. Add the API implementation from [identity-api-user](https://github.com/wso2/identity-api-user/) or 
[identity-api-server](https://github.com/wso2/identity-api-server/) under the corresponding `server` tag. 
Or if adding new resource create `jaxrs:server` configuration.

    ```
    <jaxrs:server id="server" address="/server/v1">
        <jaxrs:serviceBeans>
            <bean class="org.wso2.carbon.identity.rest.api.server.challenge.v1.ChallengesApi"/>
        </jaxrs:serviceBeans>
        <jaxrs:providers>
            <bean class="com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider"/>
        </jaxrs:providers>
    </jaxrs:server>
    <jaxrs:server id="users" address="/users/v1">
        <jaxrs:serviceBeans>
            <bean class="org.wso2.carbon.identity.rest.api.user.association.v1.UsersApi"/>
            <bean class="org.wso2.carbon.identity.rest.api.user.challenge.v1.UserIdApi"/>
            <bean class="org.wso2.carbon.identity.rest.api.user.challenge.v1.MeApi"/>
        </jaxrs:serviceBeans>
        <jaxrs:providers>
            <bean class="com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider"/>
        </jaxrs:providers>
    </jaxrs:server>
    ```
5. Build the component.
    ```
     mvn clean install
    ```

#### Deploy and test

1. Once the build completed, rename the `api-respources-<profile>.war` in the `components/<profile-module>/target/` 
into `api.war` and copy to`[IS_HOME]/repository/deployment/server/webapps/` location and restart the server. (If already exploded `api` 
folder exists in the location, remove it before restarting)

    ```
        ── <profile-module>
           └── target
               ├── api-respources-<profile>.war
    ```
2. Access the API 

    1. User APIs ```https://localhost:9443/api/users/v1/<resource>/```
    
        Sample endpoint:
        ```
            https://localhost:9443/api/users/v1/me/challenges
        ```
        Sample Request:
        ```
            curl -u admin:admin -v -X GET "https://localhost:9443/api/users/v1/me/challenges" -H "accept: application/json" -k
        ```
    
    2. Server APIs ```https://localhost:9443/api/server/v1/<resource>/```
    
        Sample endpoint:
        ```
            https://localhost:9443/api/server/v1/challenges
        ```
        Sample Request:
        ```
            curl -u admin:admin -v -X GET "https://localhost:9443/api/server/v1/challenges" -H "accept: application/json" -k
        ```


*  Refer [identity-api-user](https://github.com/wso2/identity-api-user/) repository for the implementation 
of user APIs

*  Refer [identity-api-server](https://github.com/wso2/identity-api-server/) repository for the implementation 
of server APIs