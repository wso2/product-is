==========================
WSO2 Identity Server 5.1.0
==========================

---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/product-is/badge/icon)](https://wso2.org/jenkins/job/product-is) |


---

Welcome to the WSO2 Identity Server 5.1.0 release.

WSO2 Identity Server is an open source Identity and Entitlement management server. It supports a wide array of authentication 
protocols such as SAML 2.0 Web SSO, OpenID, OAuth 2.0/1.0a, OpenID Connect and WS-Federation Passive. 
It supports role based authorization and fined grained authorization with XACML 2.0/3.0 while inbound/outbound provisioning is 
supported through SCIM and SPML.

This is based on the revolutionary WSO2 Carbon framework. All the major features have been developed as pluggable Carbon components.

New Features
============

*   Workflow support for Identity Server

        Engage workflows for any user/role operations carried out using WSO2 IS Management Console.
        For example, when a new user gets registered with WSO2 IS, a workflow will automatically be triggered and
        s/he will be assigned to a particular user role.

*   FIDO compliance

    Fast Identity Online (FIDO) is a specification developed to reduce the reliance on password for user
    authentication. The standard will enable any Web/cloud application to interface with a variety of FIDO-enabled
    security devices.

*   Link multiple user accounts

    In cases where users will have multiple entries in their respective user stores, to avoid requiring multiple
    logins to an application to obtain a fully privileged view for a single user's details, we now support merging
    of multiple user profiles.

*   PATCH operation support for SCIM 1.1

    PUT supports the replace operation but not the update operation. Since group is a heavy resource, the
    operation that alters without replacement (PATCH) needs to be implemented. If not each time a new user is
    added to the group, all the users should be sent in the PUT request.</p>

*   SAML 2.0 Bearer Token Renewal

    In IS 5.0.0, STS feature supports renewing Bearer type SAML 1.1 tokens only;
    attempts to renew Bearer type SAML 2.0 Tokens get failed. With IS 5.1.0 product will
    facilitate renewing expired Bearer type SAML 2.0 Tokens.

*   OpenID Connect Core 1.0 Compliance

    The IS 5.0.0 had OpenID Support, however there were many points in the spec that were being violated.
    Now that the specification is finalized we have made IS 5.1.0 OpenID Connect support specification
    compliant. A major improvement this area is support for IDToken response type from the
    OpenID Connect authorization endpoint.

*   Ability to notify external endpoints when changes are made to Identities</li>

    @product.name@ is now able to send invalidation notifications to external endpoints when there is a change in user
    roles, permissions or attributes as well as clear the internal cache when user roles, permissions or attributes
    been updated

Other Key Features
=============

*  Dynamically discovered federation
*  Identity Bridge - translation between heterogeneous Identity authentication protocols
*  SP Initiated and IDP Initiated SAML 2.0 Web Browser SSO provider</li>
*  SAML2 Single Logout profile support
*  OpenID 2.0 Provider
*  OpenID Connect Authorization Server
*  Social login with Facebook, Google, Yahoo and Windows Live
*  XACML 3.0/2.0 based Entitlement Engine with WS-XACML support
*  OAuth 2.0/1.0a Authorization Server with OAuth 2.0/1.0a support
*  Inbound and Outbound Identity Provisioning with SCIM 1.1</li>
*  Outbound Identiy Provisioning with SPML 2.0, Salesforce and GoogleApps
*  Integrated Windows Authentication and webSEAL authentication
*  Multi-option and multi-step (multi-factor) authentication
*  Claim based Security Token Service(STS) with SAML 2.0/1.1 support.
*  Support for various types of User Stores such as JDBC, Cassandra, LDAP, Active Directory in Read/Write mode.
*  Claim Management
*  User Profiles and Profile Management
*  Separable front-end and back-end - a single front-end server can be used to administer several back-endservers
*  Identity Bridge
*  Multi-option and multi-step authentication
*  Request Path Authenticators.
*  Social Login with Facebook / Google / Microsoft Windows Live.
*  Ability to plug-in custom developed authenticators.
*  Provisioning Bridge.
*  Just-in-time provisioning.</li>
*  Ability to plug-in custom developed provisioning connectors.
*  User Dashboard.
*  SAML2 Web SSO profile Request / Response validator.
*  Remote User Store Management.
*  Custom permissions.
*  Encrypted SAML2 Assertions.
*  NTLM grant type for OAuth 2.0
*  Workflows for user management operations
*  2 factor authentication with FIDO
*  Linking 2 or more local/federated user accounts

System Requirements
===================

1. Minimum memory - 2 GB

2. Processor - Pentium 800MHz or equivalent at minimum

3. Java SE Development Kit 1.7 or higher

4. The Management Console requires full Javascript enablement of the Web browser.

5. To build WSO2 Identity Server from the Source distribution, it is also necessary that you have Maven 3 or later.

For more details see
   http://docs.wso2.org/wiki/display/IS510/Installation+Prerequisites


Project Resources
=================

* Home page          : http://wso2.com/products/identity-server
* Library            : http://wso2.org/library/identity
* Wiki               : http://docs.wso2.org/wiki/display/IS510/WSO2+Identity+Server+Documentation
* JIRA-Issue Tracker : https://wso2.org/jira/browse/IDENTITY      
* Forums             : http://stackoverflow.com/questions/tagged/wso2/
* WSO2 Developer List: dev@wso2.org

    
Installation and Running
========================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the /bin directory
3. Once the server starts, point your Web browser to https://localhost:9443/carbon/
4. User dashboard is available at https://localhost:9443/dashboard
5. For more information, see the Installation Guide


WSO2 Identity Server Distribution Directory Structure
==============================================

            CARBON_HOME
            ├── bin
            ├── dbscripts
            ├── lib
            ├── repository
            │   ├── components
            │   ├── conf
            │   │   └── identity
            │   │       ├── identity-providers
            │   │       └── service-providers
            │   ├── database
            │   ├── deployment
            │   ├── logs
            │   ├── resources
            │   │   ├── identity
            │   │   └── security
            │   └── tenants
            └── tmp


    - bin
      Contains various scripts .sh & .bat scripts.

    - dbscripts
      Contains the database creation & seed data population SQL scripts for
      various supported databases.

    - lib
      Contains the basic set of libraries required to startup Carbon.

    - repository
      The repository where Carbon artifacts & Axis2 services and 
      modules deployed in WSO2 Carbon are stored. 
      In addition to this other custom deployers such as
      dataservices and axis1services are also stored.

    	- components
          Contains all OSGi related libraries and configurations.

        - conf
          Contains server configuration files. Ex: axis2.xml, carbon.xml

	        - identity
	          Contains all configurations related to identity.

	            - identity-providers
	              Identity providers configured using file

	            - service-providers
	              Service providers configured using file

        - database
          Contains the WSO2 Registry & User Manager database.

        - deployment
          Contains server side and client side Axis2 repositories. 
	      All deployment artifacts should go into this directory.

        - logs
          Contains all log files created during execution.

        - resources
          Contains additional resources that may be required.

	- tenants
	  Directory will contain relevant tenant artifacts 
	  in the case of a multitenant deployment.

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property.


    - LICENSE.txt
      Apache License 2.0 under which WSO2 Carbon is distributed.

    - README.txt
      This document.

    - release-notes.html
      Release information for WSO2 Carbon ${carbon.product.version}.

Secure sensitive information in carbon configuration files
----------------------------------------------------------

There are sensitive information such as passwords in the carbon configuration. 
You can secure them by using secure vault. Please go through following steps to 
secure them with default mode. 

1. Configure secure vault with default configurations by running ciphertool 
	script from bin directory.  

> ciphertool.sh -Dconfigure   (in UNIX)  

This script would do following configurations that you need to do by manually 

(i) Replaces sensitive elements in configuration files,  that have been defined in
		 cipher-tool.properties, with alias token values.  
(ii) Encrypts plain text password which is defined in cipher-text.properties file.
(iii) Updates secret-conf.properties file with default keystore and callback class. 

cipher-tool.properties, cipher-text.properties and secret-conf.properties files 
			can be found at repository/conf/security directory. 

2. Start server by running wso2server script from bin directory

> wso2server.sh   (in UNIX)

By default mode, it would ask you to enter the master password 
(By default, master password is the password of carbon keystore and private key) 

3. Change any password by running ciphertool script from bin directory.  

> ciphertool -Dchange  (in UNIX)

For more details see
http://docs.wso2.org/wiki/display/Carbon420/WSO2+Carbon+Secure+Vault

Support
=======
We are committed to ensuring that your enterprise middleware deployment is completely supported from
evaluation to production. Our unique approach ensures that all support leverages our open development
methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity, visit http://wso2.com/support/.


For more information on WSO2 Carbon, visit the WSO2 Oxygen Tank (http://wso2.org)

For more information about WSO2 Identity Server please see http://wso2.org/projects/identity or visit the
WSO2 Oxygen Tank developer portal for addition resources.

For further details, see the WSO2 Carbon documentation at
http://docs.wso2.org/wiki/display/Carbon420/WSO2+Carbon+Documentation

---------------------------------------------------------------------------
(c) Copyright 2015 WSO2 Inc.
