README
================================

Intro
---------
This describes the steps to configure and run the sample authenticators with Identity Server. 

Prerequisites
--------------

Build Identity Server 5.5.0

Tomcat 7 or above - To deploy the sample authenticator web app


Configuration
--------------

1. Copy org.wso2.carbon.identity.sample.extension.authenticators-<VERSION_NUMBER>.jar to 
<IS_SERVER_HOME>/repository/components/dropins directory.

2. Add the following entries under <AuthenticatorConfigs> in <IS_SERVER_HOME>/repository/conf/application-authentication.xml file.

<AuthenticatorConfigs>
...
...
    <AuthenticatorConfig name="SampleHardwareKeyAuthenticator" enabled="true" />
    <AuthenticatorConfig name="SampleFingerprintAuthenticator" enabled="true" />
    <AuthenticatorConfig name="SampleRetinaAuthenticator" enabled="true" />
    <AuthenticatorConfig name="RequestAttributeExtractor" enabled="true">
        <Parameter name="Headers">User-Agent,Host</Parameter>
    </AuthenticatorConfig>
    
</AuthenticatorConfigs>

3. Copy the sample-auth.war in the target folder to <IS_SERVER_HOME>/repository/deployment/server/webapp folder.

4. Restart WSO2 Identity Server.

5. Following authenticators will be available under local authenticators after above configurations.

    Sample Fingerprint Authenticator
    Sample HardwareKey Authenticator
    Sample Retina Authenticator


