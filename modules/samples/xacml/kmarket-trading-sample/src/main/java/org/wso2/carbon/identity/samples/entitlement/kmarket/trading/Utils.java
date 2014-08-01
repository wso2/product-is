/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.samples.entitlement.kmarket.trading;

import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
public class Utils {


    /**
     * reads values from config property file
     * @return return properties
     */
    public static Properties loadConfigProperties() {

        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            File file = new File((new File(".")).getCanonicalPath() + File.separator +
                                                 "src" + File.separator + "main" + File.separator +
                                                 "resources" + File.separator + "config.properties");            
            if(file.exists()){
               inputStream = new FileInputStream(file);
            } else {
                System.err.println("File does not exist : " + "config.properties");
            }
        } catch (FileNotFoundException e) {
            System.err.println("File can not be found : " + "config.properties");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Can not create the canonical file path for given file : " + "config.properties");
            e.printStackTrace();
        }

        try {
            if(inputStream != null){
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("Error loading properties from config.properties file");
            e.printStackTrace();
        } finally {
            try {
                if(inputStream!= null){
                    inputStream.close();
                }
            } catch (IOException ignored) {
                System.err.println("Error while closing input stream");
            }
        }

        if(properties.isEmpty()){
            System.out.println("No configurations are loaded.  Using default configurations");    
        }
        return properties;
    }

    public static String createXACMLRequest(String userName, String resource, int amount, int totalAmount){

        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">buy</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + userName +"</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"http://kmarket.com/category\">\n" +
                "<Attribute AttributeId=\"http://kmarket.com/id/amount\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + amount + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "<Attribute AttributeId=\"http://kmarket.com/id/totalAmount\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + totalAmount + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "</Request>";

    }

    public static void setup (WSO2IdentityAgent agent){

        String policyDirectoryPath = System.getProperty("user.dir") + File.separator + "resources";
        File rootDirectory = new File(policyDirectoryPath);
        File[] policyFiles = rootDirectory.listFiles();
        if(policyFiles != null) {
            for(int i = 1; i < policyFiles.length + 1; i++){
                if(policyFiles[i-1].exists() && policyFiles[i-1].isFile()){
                    try{
                        FileInputStream inputStream = new FileInputStream(policyFiles[i-1]);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder builder = new StringBuilder();
                        String read = "";
                        while (read != null){
                            builder.append(read);
                            read = reader.readLine();
                        }

                        PolicyDTO policyDTO = new PolicyDTO();
                        policyDTO.setPolicy(builder.toString());
                        agent.uploadPolicy(policyFiles[i-1].getName(), builder.toString());
                        inputStream.close();
                        reader.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        agent.setUpUserAndRoles();
    }
}
