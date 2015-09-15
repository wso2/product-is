<<<<<<< HEAD
<a href="http://wso2.com/products/identity-server/">
<img src="http://b.content.wso2.com/sites/all/common/images/product-logos/identity-server.svg"
     srcset="http://b.content.wso2.com/sites/all/common/images/product-logos/identity-server.svg@2x.png 2x"
     alt="WSO2 Identity Server" />
</a>

Welcome to WSO2 Identity Server
===============================

---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/product-is/badge/icon)](https://wso2.org/jenkins/job/product-is) |



[![Join the chat at https://gitter.im/wso2/product-identity-server](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/wso2/product-identity-server?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

---

WSO2 Identity Server is an open source Identity and Entitlement management server. It supports a wide array of authentication 
protocols such as SAML 2.0 Web SSO, OpenID, OAuth 2.0/1.0a, OpenID Connect and WS-Federation Passive. 
It supports role based authorization and fined grained authorization with XACML 2.0/3.0 while inbound/outbound provisioning is 
supported through SCIM and SPML.

This is based on the revolutionary WSO2 Carbon framework. All the major features have been developed as pluggable Carbon components.

New Features
============

C5 User Portal

Other Key Features
=============


System Requirements
===================

1. (memory)

2. (processor)

3. JDK 1.8

4. (browser/javascript)

For more details see (link to docs)

Building Identity Server from Source
====================================

https://docs.wso2.com/display/IS600/Downloading+and+Running+the+Product

Project Resources
=================

* Home page           : http://wso2.com/products/identity-server
* Library             : http://wso2.org/library/identity
* Documentation       : https://docs.wso2.com/display/IS600/WSO2+Identity+Server+Documentation
* JIRA-Issue Tracker  : https://wso2.org/jira/browse/IDENTITY
* Forums              : http://stackoverflow.com/questions/tagged/wso2/
* WSO2 Developer List : dev@wso2.org
* Carbon Documentation:
* WSO2 Home Page      : http://wso2.org

Installation and Running
========================

https://docs.wso2.com/display/IS600/Downloading+and+Running+the+Product

Upgrading from a Previous Version
=================================

(link to docs)

Known Issues
============

All known issues have been recorded at

Issues Fixed In This Release
============================

All issues fixed for WSO2 Identity Server 6.0.0-m1 release have been recorded at https://wso2.org/jira/issues/?filter=13640


WSO2 Identity Server Distribution Directory Structure
==============================================

            CARBON_HOME
            ├── bin
            ├── conf
            │   ├── data-bridge
            │   ├── datasources
            │   ├── etc
            │   ├── identity
            │   │   └── challenge-questions
            │   ├── osgi
            │   ├── security
            │   └── transports
            ├── database
            ├── dbscripts
            │   ├── authorization
            │   ├── connector
            │   ├── identity-mgt
            │   └── metrics
            ├── deployment
            ├── logs
            ├── osgi
            │   ├── dropins
            │   ├── features
            │   ├── p2
            │   ├── plugins
            │   └── profiles
            ├── resources
            │   └── security
            └── tmp


    - bin
        Contains various scripts .sh & .bat scripts.

    - conf
        Contains all the Carbon configuration files.
            - data-bridge
            - datasources
                Configuration files for configure the datasources used for identity server database.
            - etc
                - pax-logging.properties
                    This file contains the configuration properties required for initializing the pax-logging framework.
            - identity
                Contains all configurations related to identity.
            - osgi
                - launch.properties
                    This file contains the WSO2 Carbon osgi runtime configuration overrides. The properties defined here are loaded prior to starting the framework and can also be used to override System Properties.
                - osgi-debug.options
                    This file contains the debug options for WSO2 Carbon osgi runtime.
            - security
                Configuration files related to security configurations in identity server.
            - transports
                Transport level configuration files are resides here.
            - carbon.yml
                The Carbon server configuration file.
            - deployment.yml
            - log4j2.xml
                The log4j2 configuration file used by WSO2 Carbon.
            - metrics.properties
            - metrics.yml

    - database
        Contains the WSO2 Registry & User Manager database.
    - dbscripts
        Contains the database creation & seed data population SQL scripts for various supported databases.
            - connector
            - identity-mgt
            - metrics
    - deployment
        Contains server side and client side Axis2 repositories. All deployment artifacts should go into this directory.
    - logs
        Contains all log files created during execution.
    - osgi
        - dropins
            If you have OSGi bundles that should be added to Carbon, copy those into this directory.
        - features
        - p2
            Contains Carbon provisioning (p2) related configuration files.
        - plugins
            This contains all OSGi bundles that are used to run the server.
        - profiles
        - artifacts.xml
    - resources
        Contains additional resources that may be required.
    - security
    - tmp
        Used for storing temporary files, and is pointed to by the java.io.tmpdir System property.

Secure sensitive information in carbon configuration files
----------------------------------------------------------

(link to docs)

Support
=======

(link to support - http://wso2.com/support/)

Crypto Notice
=============

This distribution includes cryptographic software.  The country in
which you currently reside may have restrictions on the import,
possession, use, and/or re-export to another country, of
encryption software.  Before using any encryption software, please
check your country's laws, regulations and policies concerning the
import, possession, or use, and re-export of encryption software, to
see if this is permitted.  See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms.  The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included cryptographic
software:

Apacge Rampart   : http://ws.apache.org/rampart/
Apache WSS4J     : http://ws.apache.org/wss4j/
Apache Santuario : http://santuario.apache.org/
Bouncycastle     : http://www.bouncycastle.org/

---------------------------------------------------------------------------
(c) Copyright 2017 WSO2 Inc.
=======
WSO2 IDENTITY SERVER


WSO2 Identity Server provides sophisticated security and identity management of enterprise web applications, services, and APIs, and makes life easier for developers and architects with its hassle-free, minimal monitoring and maintenance requirements. In its latest version, Identity Server acts as an Enterprise Identity Bus (EIB) — a central backbone to connect and manage multiple identities regardless of the standards on which they are based.In addition to using role-based access control (RBAC) convention, fine-grained policy based access control, and SSO bridging to make identity and entitlement management effortless, the all-new version of Identity Server now includes features such as identity token transformation and mediation for seamless integration between internal applications and cloud apps such as Salesforce, Google Apps, and Microsoft Office 365; new user and group provisioning capabilities; and multi-option and multi-step authentication to provide flexibility in selecting authentication options and enable robust multi-factor authentication.
================
>>>>>>> 0f1ddfb... adding readme content
