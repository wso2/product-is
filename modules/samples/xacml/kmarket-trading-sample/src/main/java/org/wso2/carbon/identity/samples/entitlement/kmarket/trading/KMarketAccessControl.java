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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;

import javax.xml.namespace.QName;
import java.io.Console;
import java.util.*;

/**
 * On-line trading sample
 */
public class KMarketAccessControl {

    private static WSO2IdentityAgent agent;

    private static Map<String,String> priceMap = new HashMap<String, String>();

    private static Map<String,String> idMap = new HashMap<String, String>();

    private static String products = "[1] Food ($20.00)\t[2] Drink ($5.00)\t[3] Fruit ($15.00)\t[4] " +
            "Liquor ($80.00)\t [5] Medicine ($50.00)\n";

    public static void main(String[] args){

        // create agent instances
        agent = new WSO2IdentityAgent(Utils.loadConfigProperties());

        if(args != null && args.length == 1 && "setup".equals(args[0])){
            System.out.println("\nStarting the K-Market sample setup\n");
            Utils.setup(agent);
            System.out.println("\nFinishing the K-Market sample setup\n");
            System.exit(0);
        }

        Console console;
        String userName = null;
        String password = null;
        String productName = null;
        int numberOfProducts = 1;
        int totalAmount;

        // print description
        printDescription();

        // init basic data
        initData();



        System.out.println("\nPlease login to K-market trading system\n");

        if ((console = System.console()) != null){
            userName = console.readLine("Enter User name : ");
            if(userName == null || userName.trim().length() < 1 ){
                System.err.println("\nUser name can not be empty\n");
                System.exit(0);
            }

            char[] passwordData;
            if((passwordData = console.readPassword("%s", "Enter User Password :")) != null){
                password = String.valueOf(passwordData);
            } else {
                System.err.println("\nPassword can not be empty\n");
                System.exit(0);
            }

            if(agent.authenticate(userName, password)){
                System.out.println("\nUser is authenticated Successfully\n");
            } else {
                System.err.println("\nUser is NOT authenticated\n");
                System.exit(0);
            }
        }

        System.out.println("\nYou can select one of following items for your shopping chart : \n");

        System.out.println(products);    

        if ((console = System.console()) != null){

            String productId = console.readLine("Enter Product Id : ");
            if(productId == null || productId.trim().length() < 1 ){
                System.err.println("\nProduct Id can not be empty\n");
                System.exit(0);
            } else {
                productName = idMap.get(productId);
                if(productName == null){
                    System.err.println("\nEnter valid product Id\n");
                    System.exit(0);
                }
            }

            String productAmount = console.readLine("Enter No of Products : ");
            if(productAmount == null || productAmount.trim().length() < 1 ){
                numberOfProducts = 1;
            } else {
                numberOfProducts = Integer.parseInt(productAmount);
            }
        }

        totalAmount = calculateTotal(productName, numberOfProducts);
        System.out.println("\nTotal Amount is  : " + totalAmount + "\n");

        System.out.println("\nStarting Transaction ..........\n");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String request = Utils.createXACMLRequest(userName, productName, numberOfProducts, totalAmount);

        System.out.println("\n======================== XACML Request ====================");
        System.out.println(request);
        System.out.println("===========================================================");

        String response = agent.authorize(request);

        System.out.println("\n======================== XACML Response ===================");
        System.out.println(response);
        System.out.println("===========================================================");

        if(response != null){
            OMElement advice = null;
            String simpleDecision = null;
            String reason = null;
            try{
                OMElement decisionElement = AXIOMUtil.stringToOM(response);
                if(decisionElement != null){
                    OMElement result = decisionElement.getFirstChildWithName(new QName("Result"));
                    if(result != null){
                        simpleDecision = result.getFirstChildWithName(new QName("Decision")).getText();
                        advice = result.getFirstChildWithName(new QName("AssociatedAdvice"));
                    }
                }

                if("Permit".equals(simpleDecision)){
                    System.out.println("\nTransaction was completed successfully\n");
                    System.exit(0);
                }

                if("Deny".equals(simpleDecision)){
                    if(advice != null){
                        Iterator iterator =  advice.getChildElements();
                        // only takes 1st advice and attribute assignment.
                        if(iterator.hasNext()){
                            OMElement element = (OMElement) iterator.next();
                            Iterator attributeIterator = element.getChildElements();
                            if(attributeIterator.hasNext()){
                                OMElement attribute = (OMElement) attributeIterator.next();
                                reason = attribute.getText();
                            }

                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            System.err.println("\nYou are NOT authorized to perform this transaction\n");
            if(reason != null){
                System.err.println("Due to " + reason + "\n");     
            }
        } else {
            System.err.println("\nInvalid authorization response\n");
        }
        System.exit(0);
    }

    private static void initData(){

        idMap.put("1" , "Food");
        idMap.put("2" , "Drink");
        idMap.put("3" , "Fruit");
        idMap.put("4" , "Liquor");
        idMap.put("5" , "Medicine");

        priceMap.put("Food" , "20");
        priceMap.put("Drink" , "5");
        priceMap.put("Fruit" , "15");
        priceMap.put("Liquor" , "80");
        priceMap.put("Medicine" , "50");
    }


    public static int calculateTotal(String productName, int amount){

        String priceString = priceMap.get(productName);
        return Integer.parseInt(priceString)*amount;

    }

    public static void printDescription(){

        System.out.println("\nK-Market is on-line trading company. They have implemented some access " +
                "control over the on-line trading using XACML policies. K-Market has separated their " +
                "customers in to three groups and has put limit on on-line buying items.\n");

    }
}
