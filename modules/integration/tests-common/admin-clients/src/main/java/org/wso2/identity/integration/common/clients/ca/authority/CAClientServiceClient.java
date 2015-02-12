///*
// * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package org.wso2.identity.integration.common.clients.ca.authority;
//
//import org.apache.axis2.AxisFault;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.wso2.carbon.identity.certificateauthority.stub.*;
//import org.wso2.identity.integration.common.clients.AuthenticateStub;
//
//import java.rmi.RemoteException;
//
//public class CAClientServiceClient {
//    private static final Log log = LogFactory.getLog(CAClientServiceClient.class);
//
//    private final String serviceName = "CAClientService";
//    private CAClientServiceStub caClientServiceStub;
//    private String endPoint;
//
//    public CAClientServiceClient(String backEndUrl, String sessionCookie)
//            throws AxisFault {
//        this.endPoint = backEndUrl + serviceName;
//        caClientServiceStub = new CAClientServiceStub(endPoint);
//        AuthenticateStub.authenticateStub(sessionCookie, caClientServiceStub);
//    }
//
//    public CAClientServiceClient(String backEndUrl, String userName, String password)
//            throws AxisFault {
//        this.endPoint = backEndUrl + serviceName;
//        caClientServiceStub = new CAClientServiceStub(endPoint);
//        AuthenticateStub.authenticateStub(userName, password, caClientServiceStub);
//    }
//
//    public String addCsr(String encodedCsr) throws RemoteException, CAClientServiceCaException {
//        return caClientServiceStub.addCsr(encodedCsr);
//    }
//
//    public CsrDTO getCsr(String serialNo) throws RemoteException, CAClientServiceCaException {
//        return caClientServiceStub.getCsr(serialNo);
//    }
//
//    public CertificateDTO getCertificate(String serialNo) throws RemoteException, CAClientServiceCaException {
//        return caClientServiceStub.getCertificate(serialNo);
//    }
//
//    public CsrMetaInfo[] getCsrList() throws RemoteException, CAClientServiceCaException {
//        return caClientServiceStub.getCsrList();
//    }
//
//    public void revokeCertificate(String serial, int reason){
//        //todo
//    }
//}
