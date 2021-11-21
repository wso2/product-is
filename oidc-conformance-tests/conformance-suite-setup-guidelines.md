
## Setting up the Conformance Suite Locally

### Prerequisites

* Java 11
* Git
* Maven
* Docker

1. Clone the conformance suite repository

    ```   
    git clone https://gitlab.com/openid/conformance-suite.git
    cd conformance-suite
    ```

2. Compile and package the conformance suite

    ```
    mvn clean package -Dmaven.test.skip=true
    ```

3. Configure docker to resolve internal host name of wso2 identity server
   (Find out the IP address of your host machine and add it as extra hosts under the `mongodb`, `httpd` and `server` sections as follows in the docker-compose-dev.yml file).

    ```
    version: '3'`
    services:
    mongodb:
        image: mongo:4.2
        volumes:
        - ./mongo/data:/data/db
        extra_hosts:
        - "localhost.com:<HOST_MACHINE_IP>"

    httpd:
        build:
          context: ./httpd
          dockerfile: Dockerfile-static
        ports:
         - "8443:8443"
        extra_hosts:
         - "localhost.com:<HOST_MACHINE_IP>"
        volumes:
         - ./src/main/resources/:/usr/local/apache2/htdocs/
        depends_on:
         - server
    server:
        build:
          context: ./server-dev
        ports:
         - "9999:9999"
        extra_hosts:
         - "localhost.com:<HOST_MACHINE_IP>"
    ```
4. Start the conformance suite in development mode
    
    ```
    docker-compose -f docker-compose-dev.yml up
    ```

Conformance suite will be available at https://localhost:8443.