README
================================

Intro
---------
This describes the steps to configure and run the sample authenticators with Identity Server. 

Prerequisites
--------------

Build Identity Server 


Configuration
--------------

1. Copy org.wso2.carbon.identity.sample.extension.authenticators-<VERSION_NUMBER>.jar to 
<IS_SERVER_HOME>/repository/components/dropins directory.

2. Add the following entries under <AuthenticatorConfigs> in <IS_SERVER_HOME>/repository/conf/application-authentication.xml file.

```
<AuthenticatorConfigs>
...
...
    <AuthenticatorConfig name="DemoHardwareKeyAuthenticator" enabled="true" />
    <AuthenticatorConfig name="DemoFingerprintAuthenticator" enabled="true" />
    <AuthenticatorConfig name="DemoFaceIdAuthenticator" enabled="true" />
    
</AuthenticatorConfigs>

```
3. Copy the sample-auth.war in the target folder to <IS_SERVER_HOME>/repository/deployment/server/webapp folder.

4. Restart WSO2 Identity Server.

5. Following authenticators will be available under local authenticators after above configurations.

    Demo Fingerprint Authenticator
    Demo HardwareKey Authenticator
    Demo Retina Authenticator


