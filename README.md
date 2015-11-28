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

Features
===========

* Identity Bridge -
  WSO2 Identity Server 5.1.0 is capable of translating between heterogeneous authentication protocols and transforming 
  and mediating any identity assertion, between SAML2.0, OAuth 1.0a/2.0, OpenID, OpenID Connect, WS-Federation Passive. 
  This leads to seamless integration between internal applications and cloud applications such as Salesforce, Google Apps 
  and Office 365.

  Few example use cases.

  - Login to your corporate Salesforce or Google Apps accounts from your Facebook credentials.
  - Login to your laptop connected to your corporate Windows domain - you will be automatically logged into your corporate Salesforce and Google Apps accounts.
  - Login to corporate Office 365 account - you will be automatically logged into you Google Apps and Salesforce accounts.
  - Login to Identity Server user portal - you will be automatically logged into your internal Liferay portals.

* Multi-option and multi-step authentication -

  With WSO2 Identity Server 5.1.0, a given service provider (irrespective of the protocol it supports) can select the 
  login options it wants to present to it users. Authentication can be a mix of two types: Multi-option login and 
  multi-step login. With multi-option authentication - the user will be presented with a login screen to pick whatever the authentication mechanism he/she wants (e.g.: Username/password, Social login with Facebook, Login with a federated Home Identity Provider). A service provider can also choose to have multi-step authentication. Multi step authentication can naturally be extended to be multi-factor depending on the kind of authentication. 

* Request Path Authenticators.

  The responsibly of getting the user's credentials is left to the service provider, and the credentials will be 
  attached to the request to the Identity Server. 

* Social Login with Facebook / Google / Microsoft Windows Live.

  Facebook / Google / Microsoft Windows Live authenticators are the very first authenticators we are shipping with 
  Identity Server 5.1.0 for social login. 

* Ability to plug-in custom developed authenticators.

  Owing to the power of OSGi, custom authenticators can be easily written and put into the Identity Server which 
  will pick them dynamically and execute. The UI will also be extended with property fields for administrators to 
  configure the authenticators if they are federated authenticators.

* Provisioning Bridge.

  WSO2 Identity Server 5.1.0 is capable of accepting inbound provisioning requests based on SOAP or SCIM, 
  provisioning to the Resident Identity Provider using LDAP, Active Directory, JDBC or Carbon Remote UserStoreManagement 
  user management APIs, and transform them to outbound provisioning requests based on SCIM, SPML 2.0, Salesforce, 
  Google Apps provisioning APIs.

* Just-in-time provisioning.

  In the process of federated login WSO2 Identity Server is capable of provisioning the corresponding subject to a 
  preferred user store domain as well as other configured Identity Providers.

* Ability to plug-in custom developed provisioning connectors.

  Like with authenticators provisioning connectors also can be added to the system by implementing the relevant 
  APIs and dropping them to the system as OSGi bundles
  
* User Dashboard.

  WSO2 Identity Server 5.1.0 ships with a jaggery based user dashboard to expose end user functionality. 
  This dashboard is completely decoupled from the management console, and is completely extensible due to the fact that 
  it is able to render gadgets. The application can run from within IS or as a separate jaggery app on AS.

* SAML2 Web SSO profile Request / Response validator.

  A UI based tool in the Identity Server 5.1.0 management console for debugging the SAML2 Web SSO requests and responses.

* Remote User Store Management.

  WSO2 Carbon's User/Group Administration SOAP API is also now supported as part of the UserStoreManager 
  implementations shipped by default. This means heterogeneous user stores distributed across different data centers 
  can be managed from a single Identity Server node.

* Custom permissions.

  Application specific permissions can be added by service providers and assigned to roles, and by using the 
  remote authorization API do permission based access control for users.

* Encrypted SAML2 Assertions.

  The SAML2 assertion issued for SAML2 based SSO login can now have encrypted assertions. To decrypt the assertion 
  the service provider will have to have the Identity Server tenant's public certificate

* NTLM grant type for OAuth 2.0

* Introducing the workflow feature to the WSO2 IS is required in order to add more control to the tasks that are
  executed in it. For example, with the workflow feature you can add another constraint to the ‘User Add’ operation in
  the identity server. These are workflows where the operations go through a predefined path. These types of tasks can be done through the WSO2 Business Process Server (WSO2 BPS) but this new feature adds that capability to the identity server as well.

Other Key Features
=============

* Dynamically discovered federation
* Identity Bridge - translation between heterogeneous Identity authentication protocols
* SP Initiated and IDP Initiated SAML 2.0 Web Browser SSO provider
* SAML2 Single Logout profile support
* OpenID 2.0 Provider
* OpenID Connect Authorization Server
* Social login with Facebook, Google, Yahoo and Windows Live
* XACML 3.0/2.0 based Entitlement Engine with WS-XACML support
* OAuth 2.0/1.0a Authorization Server with OAuth 2.0/1.0a support
* Inbound authentication with SCIM 1.1
* Outbound provisioning with SCIM 1.1, SPML 2.0, Salesforce and GoogleApps
* Integrated Windows Authentication and webSEAL authentication
* Multi-option and multi-step (multi-factor) authentication
* Claim based Security Token Service(STS) with SAML 2.0/1.1 support
* Support for various types of User Stores such as JDBC, Cassandra, LDAP, Active Directory in Read/Write mode
* Claim Management
* User Profiles and Profile Management
* Separable front-end and back-end - a single front-end server can be used to administer several back-end servers

System Requirements
===================

1. Minimum memory - 1 GB

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
        |-- bin <directory>
        |-- dbscripts <directory>
        |-- lib <directory>
        |-- repository <directory>
        |   |-- components <directory>
        |   |-- conf <directory>
	|   |	|-- identity <directory>
	|   |	|   |-- identity-providers
	|   |	|   |-- service-providers
        |   |-- data <directory>
        |   |-- database <directory>
        |   |-- deployment <directory>
        |   |-- logs <directory>
        |   |-- resources <directory>
        |   |   |-- security <directory>
        |   |-- tenants <directory>
        |-- tmp <directory>
	|-- webapp-mode <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- release-notes.html <file>

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
	  Contains identity providers and service providers configured using files.

        - data
          Contains internal LDAP related data.

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

    - webapp-mode
      The user has the option of running WSO2 Carbon in webapp mode (hosted as a web-app in an application server).
      This directory contains files required to run Carbon in webapp mode. 

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

2. Start server by running wso2server sciprt from bin directory

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
