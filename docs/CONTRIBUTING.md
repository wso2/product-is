Steps to Debug WSO2 Identity Server Development Environment in Eclipse:

All WSO2 products are based on a common platform called Carbon. It is therefore essential to obtain and build this layer before attempting to build the desired product itself. The build process can be divided into three distinct steps:
1.	Build the carbon-kernel code.
2.	Build the carbon-identity-framework code.
3.	Build the WSO2 identity server code.
Note: We are using the following version of the products:
  •	carbon-identity-framework – v5.7.5
  •	carbon-kernel – v 4.4.11
  •	product-is – v5.3.0
1.1.1.1	Import source code in eclipse IDE
For WSO2 products, we use developer studio plugin in eclipse. The eclipse version: Luna Service Release 2 (4.4.2) already contains this plugin from the following link: https://wso2.com/products/developer-studio/

  a)	Import source code of carbon-kernel from GitHub: We need to import carbon-kernel code into eclipse IDE from GitHub. For this we have to follow the below steps:
      o	Open eclipse.
      o	Select workspace for carbon-identity-framework
      o	Click on “File” ? Import ? Projects from Git? Clone URI-> Enter URI for carbon-identity-framework i.e. https://github.com/wso2/carbon-kernel.git and click on “Next” ? “Next” ? Enter Directory and click “Next” ? Click “Cancel”.
      o	Again click on “Import” option from file menu ?Select Existing Maven Projects and click on “Next”?Click on “Finish”.
  b)	Import source code of carbon-identity-framework from GitHub: We have to import carbon-identity-framework code into eclipse from GitHub. For this we have to follow the below steps:
      o	Open eclipse.
      o	Select workspace for carbon-identity-framework
      o	Click on “File” ? Import ? Projects from Git? Clone URI ? Enter URI for carbon-identity-framework i.e. https://github.com/wso2/carbon-identity-framework.git and click “Next” ? Next ? Enter Directory and click on “Next” ? Click “Cancel”.
      o	Again click on “Import” option from file menu ? Select Existing Maven Projects and click on “Next” ? Click on “Finish”.
  c)	Import source code of wso2 identity-server from GitHub: We have to import identity-server code into eclipse from GitHub. For this we have to follow the below steps:
      o	Open eclipse.
      o	Select workspace for identity-server
      o	Click on “File” ? Import ? Projects from Git ? Clone URI ? Enter URI for product-is i.e. https://github.com/wso2/product-is.git and click on “Next” ? Next ? Enter Directory and click “Next” ? Click “Cancel”.
      o	Again click on “Import” option from file menu ? Select Existing Maven Projects and click on “Next”? Click on “Finish”.
1.1.1.2	Build source code
  a)	Build carbon-kernel Code: To build the source code we have to follow the below steps:
      o	Right click on carbon-Kernel project and select run as option. Then Click on Maven Build option.
      o	New dialog box “Run Configurations” will be displayed. Go to “Main” tab and write clean install in “Goals” field and click on Run button.
  b)	Build carbon-identity-framework Code: To build the source code we have to follow the below steps:
      o	Right click on carbon-identity-framework project and select run as option. Then Click on Maven Build option.
      o	New dialog box will be displayed. Go to “Main” tab and write mvn clean install in “Goals” field and click on Run button.
  c)	Build identity-server Code: To build the source code we have to follow the below steps:
      o	Right click on identity-server project and select run as option. Then Click on Maven Build option.
      o	New dialog box will be displayed, run the one of the below maven commands from product-is directory,
        clean install
          (To build the binary and source distributions with the tests)
        clean install -Dmaven.test.skip=true
          (To build the binary and source distribution, without running any of the unit/integration tests).
1.1.1.3	Run the application
  a)	Run Source code: To run the project, we need to follow below steps:
      o	Right click on org.wso2.carbon.bootstrap project and select run as option. Then Click on Run Configuration option.
      o	Right click on Java Application option and click on New.
      o	Now browse the org.wso2.carbon.bootstrap project in “Project” field and search org.wso2.carbon.bootstrap.Bootstrap class for “Main Class” field.

      o	Now click on Arguments tab and write the below content in “VM arguments” field:
          -Xms256m
          -Xmx1024m
          -XX:MaxPermSize=256m
          -XX:+HeapDumpOnOutOfMemoryError
          -Dcom.sun.management.jmxremote
          -Dwso2.server.standalone=true
          -Dcatalina.base="path_to_carbon_home\lib\tomcat"
          -Dcarbon.home="path_to_carbon_home"
          -Dwso2.carbon.xml=path_to_carbon_home\repository\conf\carbon.xml
          -Dwso2.registry.xml="path_to_carbon_home\repository\conf\registry.xml"
          -Dwso2.user.mgt.xml="path_to_carbon_home\repository\conf\user-mgt.xml"
          -Dwso2.transports.xml="path_to_carbon_home\repository\conf\mgt-transports.xml"
          -Djava.util.logging.config.file="path_to_carbon_home\repository\conf\etc\logging-bridge.properties"
          -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager
          -Dcarbon.config.dir.path="path_to_carbon_home\repository\conf"
          -Dcomponents.repo="path_to_carbon_home\repository\components"
          -Dconf.location="path_to_carbon_home\repository\conf"
          -Dcom.atomikos.icatch.file="path_to_carbon_home\lib\transactions.properties"
          -Dcom.atomikos.icatch.hide_init_file_path="true"
          -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false
          -Dorg.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER=true
          -Dcom.sun.jndi.ldap.connect.pool.authentication=simple
          -Dcom.sun.jndi.ldap.connect.pool.timeout=3000
          -Dorg.terracotta.quartz.skipUpdateCheck=true
          -DworkerNode=false
          -Dorg.wso2.ignoreHostnameVerification=true
          -Dfile.encoding=UTF8
          -Dsetup=true

       Refer point 5 for carbon.home path in below Section 1.1.1.4.
      o	Once the server starts, point your Web browser to https://localhost:9443/carbon/
1.1.1.4	Troubleshooting
  1.	We checked the pom.xml of product-is to find the compatible version of the carbon identity framework and carbon-kernel.
  2.	To identify the starting point to run the code we have analyzed the wso2server.bat file in which we found that the start point of this code is from org.wso2.carbon.bootstrap class.
  3.	We have to pass multiple command line arguments while starting the application otherwise we will get exception. So for identifying those command line arguments we have to analyze the wso2server.bat file and pass those arguments while starting the application.
  4.	We get multiple dependency missing error so we have to add those dependencies manually which are given below:
      •	javax.annotation-api
      •	jta
      •	persistence-api
      •	ejb-api
      •	xercesImpl
      •	xml-apis
      •	commons-lang
  5.	While running the application we have faced many issues like missing files and plugins. To resolve these issues, we have used the binary distribution zip file, which is created after building the product. For this we have set the {carbon.home} value in the arguments as the path of binary distribution folder, which is created after building the code in product-is folder at below location
           {product-is\modules\distribution\target}
