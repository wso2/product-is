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

/// <reference types="cypress" />
import Logger from '../../../logs/logger-service'
context('Basic Carbon console visit', () => {

it('Validate visit carbon console successfully.', () => {
      cy.visit('https://localhost:9853/carbon');
      cy.wait(1000);
      cy.get('input[id="txtUserName"]').type('admin');
      cy.get('input[id="txtPassword"]').type('admin',{ log : false });
      cy.get('input.button').click({ delay: 70 });
      })
  })
