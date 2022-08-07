==========================
WSO2 Identity Server ${product.version}
==========================

Welcome to the WSO2 Identity Server ${product.version} release.

WSO2 Identity Server is an open source Identity and Access Management solution federating and managing identities across
both enterprise and cloud service environments. It supports a wide array of authentication protocols such as
SAML 2.0 Web SSO, OAuth 2.0, OpenID Connect and WS-Federation Passive. It supports role based authorization and
fine grained authorization with XACML 2.0/3.0 while inbound/outbound provisioning is supported through SCIM.

WSO2 Identity Server is developed on top of the revolutionary WSO2 Carbon platform, an OSGi based framework that provides
seamless modularity to your SOA solution via componentization.


System Requirements
===================

1. Minimum memory - 2 GB

2. Processor - 2 Core/vCPU 1.1GHz or higher.

3. Java SE Development Kit 11

4. The Management Console requires full Javascript enablement of the Web browser.

5. To build WSO2 Identity Server from the Source distribution, it is also necessary that you have Maven 3 or later.

For more details see
    https://is.docs.wso2.com/en/6.0.0/setup/installation-prerequisites


Project Resources
=================

* Home page          : http://wso2.com/products/identity-server
* Library            : http://wso2.org/library/identity
* Wiki               : https://is.docs.wso2.com/en/6.0.0
* Issue Tracker      : https://github.com/wso2/product-is/issues
* Forums             : http://stackoverflow.com/questions/tagged/wso2is/
* WSO2 Developer List: iam-dev@wso2.org

    
Installation and Running
========================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the /bin directory
3. Once the server starts, point your Web browser to https://localhost:9443/carbon/
4. User dashboard is available at https://localhost:9443/myaccount
5. For more information, see the Installation Guide
    https://is.docs.wso2.com/en/6.0.0/deploy/get-started/install/


WSO2 Identity Server Distribution Directory Structure
==============================================


            CARBON_HOME
                    |--- bin
                    |--- dbscripts
                    |--- lib
                    |--- repository
                    |       |--- components
                    |       |--- conf
                    |       |       |--- identity
                    |       |       |--- identity-providers
                    |       |       |--- service-providers
                    |       |--- database
                    |       |--- deployment
                    |       |--- logs
                    |       |--- resources
                    |       |       |--- identity
                    |       |       |--- security
                    |       |--- tenants
                    |--- tmp


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
      Release information for WSO2 Carbon ${product.version}.

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
https://is.docs.wso2.com/en/6.0.0/deploy/security/encrypt-passwords-with-cipher-tool/

Support
=======
We are committed to ensuring that your enterprise middleware deployment is completely supported from
evaluation to production. Our unique approach ensures that all support leverages our open development
methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity, visit http://wso2.com/support/.


For more information on WSO2 Carbon, visit the WSO2 Oxygen Tank (http://wso2.org)


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

Apache Rampart   : http://ws.apache.org/rampart/
Apache WSS4J     : http://ws.apache.org/wss4j/
Apache Santuario : http://santuario.apache.org/
Bouncycastle     : http://www.bouncycastle.org/

For more information about WSO2 Identity Server please see http://wso2.org/projects/identity or visit the
WSO2 Oxygen Tank developer portal for addition resources.

For further details, see the WSO2 Identity Server documentation at
https://is.docs.wso2.com/en/6.0.0

---------------------------------------------------------------------------
(c) Copyright 2022 WSO2 LLC.
