# Contributing to WSO2 Identity Server

## Debugging WSO2 Identity Server development environment in Eclipse

All WSO2 products are based on a common platform called WSO2 Carbon. It is therefore essential to obtain and build this layer before attempting to build the desired product itself. The build process can be divided into three distinct steps:

1.	Build the carbon-kernel code. (Needed only if debugging low level user store, kernel , etc)
2.	Build the carbon-identity-framework code.
3.	Build the WSO2 identity server code.

> **Note**: This was tested with the following versions of the products:
>  * carbon-identity-framework – v5.12.153
>  * carbon-kernel – v 4.4.35
>  * product-is – v5.8.0

###	Import source code in Eclipse IDE

Contributing to IS does not require Developer Studio plugin, however all WSO2 products can use the Developer Studio plugin in Eclipse. The Eclipse version (Luna Service Release 2 (4.4.2)) already contains this plugin from the following link: https://wso2.com/products/developer-studio/.

1. Import source code of carbon-kernel from GitHub: You need to import carbon-kernel code into the Eclipse IDE from GitHub. Follow the steps below to achieve this:
   *	Open Eclipse.
   *	Select workspace for carbon-identity-framework.
   *	Click on **File** - **Import** - **Projects from Git** - **Clone URI** - Enter URI for carbon-identity-framework i.e., https://github.com/wso2/carbon-kernel.git and click on **Next** - **Next** - Enter Directory and click **Next** - Click **Cancel**.
   *	Again click on the **Import** option from the file menu. Select **Existing Maven Projects** and click on **Next** - click on **Finish**.
2. Import source code of carbon-identity-framework from GitHub: We have to import carbon-identity-framework code into Eclipse from GitHub. For this we have to follow the steps below:
   *	Open Eclipse.
   *	Select workspace for carbon-identity-framework.
   *	Click on **File** - **Import** - **Projects from Git** - **Clone URI** - Enter URI for carbon-identity-framework i.e. https://github.com/wso2/carbon-identity-framework.git and click **Next** - **Next** - Enter Directory and click on **Next** - click **Cancel**.
   *	Again click on **Import** from the file menu. Select **Existing Maven Projects** and click on **Next** - click on **Finish**.
3. Import source code of WSO2 Identity Server from GitHub: We have to import the WSO2 Identity Server code into Eclipse from GitHub. For this we have to follow the steps below:
   *	Open Eclipse.
   *	Select workspace for identity-server.
   *	Click on **File** - **Import** - **Projects from Git** - **Clone URI** - Enter URI for product-is i.e., https://github.com/wso2/product-is.git and click on **Next** - **Next** - Enter Directory and click **Next** - click **Cancel**.
   *	Again click on **Import** option from the file menu. Select **Existing Maven Projects** and click on **Next** - click on **Finish**.

###	Build source code

1. Build carbon-kernel Code: To build the source code we have to follow the below steps:
   *	Right click on carbon-Kernel project and select run as option. Then Click on Maven Build option.
   *	New dialog box “Run Configurations” will be displayed. Go to “Main” tab and write clean install in “Goals” field and click on Run button.
2. Build carbon-identity-framework Code: To build the source code we have to follow the below steps:
   *	Right click on carbon-identity-framework project and select run as option. Then Click on Maven Build option.
   *	New dialog box will be displayed. Go to “Main” tab and write mvn clean install in “Goals” field and click on Run button.
3. Build identity-server Code: To build the source code we have to follow the below steps:
   *	Right click on identity-server project and select run as option. Then Click on Maven Build option.
   *	New dialog box will be displayed, run the one of the below maven commands from product-is directory,
        * clean install (To build the binary and source distributions with the tests)
        * clean install -Dmaven.test.skip=true (To build the binary and source distribution, without running any of the unit/integration tests).

###	Run and debug the application
Running the Identity server can be done in two methods.
1. Remote Debug mode
2. Running inside the eclipse

### 1.  Remote Debug mode
1. Run the WSO2 IS with debug option turned on. This will cause IS to wait until eclipse connects to the JVM running the IS.
```
./wso2server.sh -debug 5005
```
2. Configure the eclipse remote debug. Please see the article [at DZone](https://dzone.com/articles/how-debug-remote-java-applicat). Please use the port as 5005.



### 2.  Running inside the eclipse
1. Run the source code: To run the project, you need to follow the steps below:
   *	Right click on the `org.wso2.carbon.bootstrap` project and select **Run**. Then click on **Run Configuration**.
   *	Right click on the **Java Application** option and click **New**.
   *	Now browse the `org.wso2.carbon.bootstrap` project in the **Project** field and search the `org.wso2.carbon.bootstrap.Bootstrap` class for the **Main Class** field.
   *	Now click on the **Arguments** tab and write the below content in the **VM arguments** field:
      ```
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
          
      ```

Refer point 5 for carbon.home path in the following section.

Once the server starts, point your Web browser to https://localhost:9443/carbon/

##	Troubleshooting

1. Check the `pom.xml` of product-is to find the compatible version of the carbon identity framework and carbon-kernel.
2. To identify the starting point to run the code, analyze the wso2server.bat file to find the start point of the code. E.g., it is from org.wso2.carbon.bootstrap class.
3. If you get any exception, you will have to pass multiple command line arguments while starting the application. For identifying those command line arguments you will have to analyze the wso2server.bat file and pass those arguments while starting the application.
4. If you get multiple dependency missing errors, try to add those dependencies manually. Some of these are given below:
   *	javax.annotation-api
   *	jta
   *	persistence-api
   *	ejb-api
   *	xercesImpl
   *	xml-apis
   *	commons-lang
5. You may face issues like missing files and plugins while running the application. To resolve these issues, you can use the binary distribution ZIP file, which will be created after building the product. For this we have set the {carbon.home} value in the arguments as the path of binary distribution folder, which is created after building the code in product-is folder at the following location: {product-is\modules\distribution\target}.
