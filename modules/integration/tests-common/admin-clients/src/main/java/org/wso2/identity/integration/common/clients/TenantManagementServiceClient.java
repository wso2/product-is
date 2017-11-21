/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class wraps the service stub of TenantMgtAdminService.
 */
public class TenantManagementServiceClient {
    private static Log log = LogFactory.getLog(TenantManagementServiceClient.class);

    private final String serviceName = "TenantMgtAdminService";
    private String endPoint;
    private TenantMgtAdminServiceStub tenantMgtAdminServiceStub;

    public TenantManagementServiceClient(String backEndURL, String sessionCookie) throws AxisFault {
        endPoint = backEndURL + serviceName;
        tenantMgtAdminServiceStub = new TenantMgtAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
    }

    public TenantManagementServiceClient(String backEndURL, String username, String password) throws AxisFault {
        endPoint = backEndURL + serviceName;
        tenantMgtAdminServiceStub = new TenantMgtAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(username, password, tenantMgtAdminServiceStub);
    }

    public void addTenant(String tenantDomain, String admin, String adminPassword, String email, String firstName,
                          String lastName)
            throws RemoteException, TenantMgtAdminServiceExceptionException {

        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setActive(true);
        tenantInfoBean.setAdmin(admin);
        tenantInfoBean.setAdminPassword(adminPassword);
        tenantInfoBean.setEmail(email);
        tenantInfoBean.setFirstname(firstName);
        tenantInfoBean.setLastname(lastName);
        tenantInfoBean.setCreatedDate(calendar);
        tenantInfoBean.setTenantDomain(tenantDomain);

        addTenant(tenantInfoBean);
    }

    public void addTenant(TenantInfoBean tenantInfoBean)
            throws RemoteException, TenantMgtAdminServiceExceptionException {

        String tenantDomain = tenantInfoBean.getTenantDomain();
        TenantInfoBean tenantInfoBeanGet = tenantMgtAdminServiceStub.getTenant(tenantDomain);
        if (!tenantInfoBeanGet.getActive() && tenantInfoBeanGet.getTenantId() != 0) {
            tenantMgtAdminServiceStub.activateTenant(tenantDomain);
            log.info("Tenant domain " + tenantDomain + " Activated successfully");
        } else if (!tenantInfoBeanGet.getActive() && tenantInfoBeanGet.getTenantId() == 0) {
            tenantMgtAdminServiceStub.addTenant(tenantInfoBean);
            tenantMgtAdminServiceStub.activateTenant(tenantDomain);
            log.info("Tenant domain " + tenantDomain + " created and activated successfully");
        } else {
            log.info("Tenant domain " + tenantDomain + " already registered");
        }
    }

    public void updateTenant(String tenantDomain, String admin, String adminPassword, String email, String firstName,
                          String lastName)
            throws RemoteException, TenantMgtAdminServiceExceptionException {

        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setActive(true);
        tenantInfoBean.setAdmin(admin);
        tenantInfoBean.setAdminPassword(adminPassword);
        tenantInfoBean.setEmail(email);
        tenantInfoBean.setFirstname(firstName);
        tenantInfoBean.setLastname(lastName);
        tenantInfoBean.setTenantDomain(tenantDomain);

        updateTenant(tenantInfoBean);
    }

    public void updateTenant(TenantInfoBean tenantInfoBean)
            throws RemoteException, TenantMgtAdminServiceExceptionException {

        tenantMgtAdminServiceStub.updateTenant(tenantInfoBean);
    }

    public void deleteTenant(String tenantDomain) throws RemoteException, TenantMgtAdminServiceExceptionException {

        tenantMgtAdminServiceStub.deleteTenant(tenantDomain);
    }

    public void activateTenant(String tenantDomain) throws RemoteException, TenantMgtAdminServiceExceptionException {

        tenantMgtAdminServiceStub.activateTenant(tenantDomain);
    }

    public void deactivateTenant(String tenantDomain) throws RemoteException, TenantMgtAdminServiceExceptionException {

        tenantMgtAdminServiceStub.deactivateTenant(tenantDomain);
    }

    public TenantInfoBean[] retrieveTenants() throws RemoteException, TenantMgtAdminServiceExceptionException {
        return tenantMgtAdminServiceStub.retrieveTenants();
    }

    public TenantInfoBean getTenant(String tenantDomain)
            throws RemoteException, TenantMgtAdminServiceExceptionException {

        return tenantMgtAdminServiceStub.getTenant(tenantDomain);
    }
}
