<a href="http://wso2.com/products/identity-server/">
<img src="http://b.content.wso2.com/sites/all/common/images/product-logos/identity-server.svg"
     srcset="http://b.content.wso2.com/sites/all/common/images/product-logos/identity-server.svg@2x.png 2x"
     alt="WSO2 Identity Server" />
</a>

Welcome to WSO2 Identity Server UI tests
========================================


 1. To run these tests, you need to download web drivers from here and extract them and put them in
 their driver folder in a drivers folder in product-is/tests/ui-tests/user-portal/src/test/resources/drivers .
         eg:chrome - chromedriver

    Download Links :
    ----------------
         Chrome : https://chromedriver.storage.googleapis.com/2.27/chromedriver_linux64.zip

 2. Change the property of the driver in the product-is/tests/ui-tests/user-portal/pom.xml
 to the specific web driver you test.

         Only: Headless and Chrome is available at the moment.
         
         
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

