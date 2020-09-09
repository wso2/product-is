/*
 *Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

 /// <reference types="Cypress" />

 // LOGIN LOCATORS
 const USERNAME_INPUT = "#usernameUserInput",
       PASSWORD_INPUT = "#password",
       CONTINUE_BUTTON = "Continue";
 // LOGOUT LOCATORS
 const HEADER_AVATAR_ICON = '[data-testid="app-header-user-avatar"]',
       LOGOUT_BUTTON = '[data-testid="app-header-dropdown-link-"]';


 //METHODS TO GET LOCATORS FOR HANDLE ELEMENTS IN LANDING PAGE.
 const loginUsernameInputField = () => cy.get(USERNAME_INPUT);
 const loginPasswordInputField = () => cy.get(PASSWORD_INPUT);
 //const loginPasswordInputField = () => cy.get(LOGIN_PASSWORD_INPUT_FIELD);
 const submitLoginButton = () => cy.contains(CONTINUE_BUTTON);
 const clickHeaderAvatarIcon = () => cy.get(HEADER_AVATAR_ICON).click();
 const clickLogoutButton = () => cy.get(LOGOUT_BUTTON).click();

 //EXPORT METHODS
 module.exports = { loginUsernameInputField,  loginPasswordInputField, submitLoginButton, clickHeaderAvatarIcon, clickLogoutButton }

