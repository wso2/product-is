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
//import org.wso2.carbon.identity.certificateauthority.stub.CAAdminServiceCaException;
//import org.wso2.carbon.identity.certificateauthority.stub.CAAdminServiceException;
//import org.wso2.carbon.identity.certificateauthority.stub.CAAdminServiceStub;
//import org.wso2.carbon.identity.certificateauthority.stub.CsrMetaInfo;
//import org.wso2.identity.integration.common.clients.AuthenticateStub;
//
//import java.rmi.RemoteException;
//
//public class CAAdminServiceClient {
//    private static final Log log = LogFactory.getLog(CAClientServiceClient.class);
//
//    private final String serviceName = "CAAdminService";
//    private CAAdminServiceStub caAdminServiceStub;
//    private String endPoint;
//
//    public CAAdminServiceClient(String backEndUrl, String sessionCookie)
//            throws AxisFault {
//        this.endPoint = backEndUrl + serviceName;
//        caAdminServiceStub = new CAAdminServiceStub(endPoint);
//        AuthenticateStub.authenticateStub(sessionCookie, caAdminServiceStub);
//    }
//
//    public CAAdminServiceClient(String backEndUrl, String userName, String password)
//            throws AxisFault {
//        this.endPoint = backEndUrl + serviceName;
//        caAdminServiceStub = new CAAdminServiceStub(endPoint);
//        AuthenticateStub.authenticateStub(userName, password, caAdminServiceStub);
//    }
//
//    public void signCSR(String serial, int days) throws RemoteException, CAAdminServiceCaException {
//        caAdminServiceStub.signCSR(serial, days);
//    }
//
//    public void rejectCSR(String serial) throws RemoteException, CAAdminServiceCaException {
//        caAdminServiceStub.rejectCSR(serial);
//    }
//
//    public void deleteCSR(String serial) throws RemoteException, CAAdminServiceCaException {
//        caAdminServiceStub.deleteCsr(serial);
//    }
//
//    public CsrMetaInfo[] getCsrList() throws RemoteException, CAAdminServiceCaException {
//        return caAdminServiceStub.getCsrList();
//    }
//
//    public void revokeCertificate(String serial, int reason) throws RemoteException, CAAdminServiceException {
//        caAdminServiceStub.revokeCert(serial, reason);
//    }
//
//    public void deleteCertificate(String serial){
//        //todo:
//    }
//}
