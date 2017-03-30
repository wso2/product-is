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

CREATE TABLE IDN_SESSION
(
  ID 	    		INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL ,
  KEY 	    		VARCHAR (100) NOT NULL,
  OPERATION 		VARCHAR(10) NOT NULL,
  SESSION_OBJECT 	BLOB,
  TIME_CREATED 		TIMESTAMP
);

CREATE TABLE IDN_CONTEXT
(
  ID 	    		INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL ,
  KEY 	    		VARCHAR (100) NOT NULL,
  OPERATION 		VARCHAR(10) NOT NULL,
  SESSION_OBJECT 	BLOB,
  TIME_CREATED 		TIMESTAMP
);