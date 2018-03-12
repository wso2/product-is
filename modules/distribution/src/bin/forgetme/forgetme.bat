REM
REM Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM WSO2 Inc. licenses this file to you under the Apache License,
REM Version 2.0 (the "License"); you may not use this file except
REM in compliance with the License.
REM You may obtain a copy of the License at
REM
REM http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied. See the License for the
REM specific language governing permissions and limitations
REM under the License.
REM

rem ----- Only set CARBON_HOME if not already set ----------------------------
setlocal enabledelayedexpansion

rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%CARBON_HOME%"=="" set CARBON_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET wsasDrive=%CARBON_HOME:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:

cd %CARBON_HOME%
call %CARBON_HOME%\repository\components\tools\forget-me\bin\forgetme.bat -d %CARBON_HOME%\repository\components\tools\forget-me\conf %*