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

const addContext = require('mochawesome/addContext');
const MAX_TEST_NAME_LENGTH = 1000;

Cypress.on('test:after:run', (test, runnable) => {
  if (test.state === 'failed') {
    const fullTestName = getFullTestName(runnable);

    const imagePath = `screenshots/${Cypress.spec.name}/${fullTestName} (failed).png`;

    addContext({ test }, imagePath);
  }
});

function getFullTestName(runnable) {
  let item = runnable;
  const name = [runnable.title];

  while (item.parent) {
    name.unshift(item.parent.title);
    item = item.parent;
  }

  return name
    .filter(Boolean)
    .map((n) => n.trim())
    .join(' -- ')
    .substring(0, MAX_TEST_NAME_LENGTH);
}

