# Identity Server Product Scenarios Sample Applications

## [01. Tomcat Instance with Sample applications]()

These scripts enable you to locally set-up a sample applications required inorder to run WSO2 Identity Server Scenario
 Tests
 
Script will download 
 - [apache-tomcat-8.5.35](https://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.35/bin/apache-tomcat-8.5.35.zip) distribution 
 - [travelocity.com](http://maven.wso2.org/nexus/content/repositories/releases/org/wso2/is/org.wso2.sample.is.sso.agent/5.7.0/org.wso2.sample.is.sso.agent-5.7.0.war) application 

Into a "target" folder and deploy the sample into tomcat instance.
Before starting the tomcat instance it will alter the configurations of the "travelocity.com" based on the provided 
ISHttpsUrl.

### - How to Run?

#### - For MacOS
    sh deploy-samples-mac.sh <ISHttpsURL>
    
    ex: sh deploy-samples-mac.sh https://wso2.com:9443
    
#### - For Linus
    sh deploy-samples-linux.sh <ISHttpsURL>

    ex: sh deploy-samples-linux.sh https://wso2.is.com:443

