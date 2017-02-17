--
-- Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

/* NOTE: Use VARCHAR(255) instead of VARCHAR(256) if the length needed is less than 256. Because 256 will require
 * two bytes to store the VARCHAR character length.
 */

CREATE TABLE IDM_USER
(
  ID                INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,
  USER_ID           VARCHAR(64)                        NOT NULL,
  DOMAIN_ID         INTEGER                            NOT NULL,
  CONNECTOR_TYPE    CHAR(1) DEFAULT 'I'                NOT NULL,
  CONNECTOR_ID      VARCHAR(64)                        NOT NULL,
  CONNECTOR_USER_ID VARCHAR(64)                        NOT NULL,
  STATE             VARCHAR(64)                        NOT NULL
);

CREATE TABLE IDM_GROUP
(
  ID                 INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,
  GROUP_ID           VARCHAR(64)                        NOT NULL,
  DOMAIN_ID          INTEGER                            NOT NULL,
  CONNECTOR_ID       VARCHAR(64)                        NOT NULL,
  CONNECTOR_GROUP_ID VARCHAR(64)                        NOT NULL
);

CREATE UNIQUE INDEX IDM_ENTITY_INDEX_1
  ON IDM_USER (USER_ID, DOMAIN_ID, CONNECTOR_TYPE, CONNECTOR_ID, CONNECTOR_USER_ID);

CREATE INDEX IDM_ENTITY_INDEX_2
  ON IDM_USER (CONNECTOR_ID, CONNECTOR_USER_ID);

CREATE INDEX IDM_ENTITY_INDEX_3
  ON IDM_USER (USER_ID);

CREATE UNIQUE INDEX IDM_ENTITY_INDEX_4
  ON IDM_GROUP (GROUP_ID, DOMAIN_ID, CONNECTOR_ID, CONNECTOR_GROUP_ID);

CREATE INDEX IDM_ENTITY_INDEX_5
  ON IDM_GROUP (CONNECTOR_ID, CONNECTOR_GROUP_ID);

CREATE INDEX IDM_ENTITY_INDEX_6
  ON IDM_GROUP (GROUP_ID);

CREATE TABLE IDM_USER_GROUP_MAPPING
(
  ID        INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,
  USER_ID   VARCHAR(64)                        NOT NULL,
  GROUP_ID  VARCHAR(64)                        NOT NULL,
  DOMAIN_ID INTEGER                            NOT NULL
)
