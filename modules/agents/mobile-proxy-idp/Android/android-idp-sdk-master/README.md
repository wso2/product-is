android-idp-sdk
===============
            Android-IDP-SDK
 
Android-IDP-SDK is a android library project, which can be utilized to implement single sign on(SSO) with android mobile applications. Android-IDP-SDK was specifically designed to work with WSO2 IS as Identity provider (IDP). There are couple of steps you have to follow before getting started 

1. WSO2 Identity server 4.6.0 or later version. Available here http://wso2.com/products/identity-server/ 

2. Download the product and go to <product_home>/bin, execute following commmand ./wso2server.sh in Linux or Windows ./wso2server.bat 

3. Navigate to oauth and create two oauth applications one for IDP proxy application and other one for your client application, you will get clien id, client secret for both applications. 

4. Go to idp_proxy_application directory and open IDP-Proxy-Application with your android IDE go to <xxxxx> class and put your client id, client secret as constants. Finally install IDP proxy application to your android device. 


      Develop SSO enable client application with Android-IDP-SDK

1. Build android library project with maven 3.1.0 or above 

2. Put target android-idp-sdk.jar file into your client application build path 

3. Put sample code segment inside your client application (You can find this from samples provided in samples directory)
 
