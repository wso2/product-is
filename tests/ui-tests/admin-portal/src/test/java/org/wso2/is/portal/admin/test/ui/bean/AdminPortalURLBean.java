/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.is.portal.admin.test.ui.bean;

/**
 * This is the bean class to retrieve different urls/pages of the admin-portal
 */
public class AdminPortalURLBean {

    private static String adminLoginPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/login";
    private static String adminPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/";
    private static String addUserPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/users/add";

    private static String editAdminPage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/users/edit";
    private static String defaultProfilePage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/users/edit?default#default";
    private static String employeeProfilePage = "https://" + System.getProperty("home") + ":" +
            System.getProperty("port") + "/admin-portal/users/edit?employee#employee";

    public static String getAdminLoginPage() {
        return adminLoginPage;
    }

    public static void setAdminLoginPage(String adminLoginPage) {
        AdminPortalURLBean.adminLoginPage = adminLoginPage;
    }

    public static String getAdminPage() {
        return adminPage;
    }

    public static void setAdminPage(String adminPage) {
        AdminPortalURLBean.adminPage = adminPage;
    }

    public static String getAddUserPage() {
        return addUserPage;
    }

    public static void setAddUserPage(String addUserPage) {
        AdminPortalURLBean.addUserPage = addUserPage;
    }

    public static String getEditAdminPage() {
        return editAdminPage;
    }

    public static void setEditAdminPage(String editAdminPage) {
        AdminPortalURLBean.editAdminPage = editAdminPage;
    }

    public static String getDefaultProfilePage() {
        return defaultProfilePage;
    }

    public static void setDefaultProfilePage(String defaultProfilePage) {
        AdminPortalURLBean.defaultProfilePage = defaultProfilePage;
    }

    public static String getEmployeeProfilePage() {
        return employeeProfilePage;
    }

    public static void setEmployeeProfilePage(String employeeProfilePage) {
        AdminPortalURLBean.employeeProfilePage = employeeProfilePage;
    }
}
