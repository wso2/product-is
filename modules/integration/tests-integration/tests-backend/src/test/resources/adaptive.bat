@echo off
rem ----------------------------------------------------------------------------
rem Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
rem
rem WSO2 Inc. licenses this file to you under the Apache License,
rem Version 2.0 (the "License"); you may not use this file except
rem in compliance with the License.
rem You may obtain a copy of the License at
rem
rem http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.

rem ----------------------------------------------------------------------------
rem Adaptive Authentication Script for the WSO2 Carbon Server
rem
rem Environment Variable Prequisites
rem
rem    CARBON_HOME   	Home of WSO2 Carbon installation. If not set I will  try
rem                    	to figure it out.
rem    NASHORN_VERSION   	OpenJDK Nashorn Version
rem                   	
rem    ASM_VERSION   	ASM Util, Commons Version.
rem
rem -----------------------------------------------------------------------------

set NASHORN_VERSION=15.3
set ASM_VERSION=9.2

set SERVER_RESTART_REQUIRED="false"

set CARBON_HOME=%1
set DISABLE=%2

set LIB_REPO=%CARBON_HOME%\repository\components\lib

setlocal enabledelayedexpansion

rem commandline arguement 'DISABLE' or 'disable' is passed 
if "%DISABLE%"=="DISABLE" goto disableAdaptiveAuth 
if "%DISABLE%"=="disable" goto disableAdaptiveAuth 
rem no commandline arguements are passed
goto enableAdaptiveAuth

:disableAdaptiveAuth
set LOCAL_NASHORN_VERSION=""
set LOCAL_ASM_VERSION=""
if exist "%CARBON_HOME%\repository\components\lib\nashorn-core-*.jar" (
  set SERVER_RESTART_REQUIRED="true"
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\nashorn-core-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_NASHORN_VERSION=%%l.%%m"
  echo Remove existing Nashorn library from lib folder: !full_artifact_name!
  del !location!
  echo Nashorn library Removed from components\lib.  
)
if exist "%CARBON_HOME%\repository\components\dropins\nashorn_core_*.jar" (
  set SERVER_RESTART_REQUIRED="true"
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\dropins\nashorn_core_!LOCAL_NASHORN_VERSION!*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  echo Remove existing Nashorn library from dropins: !full_artifact_name!
  del /f !location!
  echo Nashorn library Removed from components\dropins.  
)
if exist "%CARBON_HOME%\repository\components\lib\asm-util-*.jar" (
  set SERVER_RESTART_REQUIRED="true"
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\asm-util-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"
  echo Remove existing ASM Util library from lib folder: !full_artifact_name!
  del !location!
  echo ASM Util library Removed from components\lib.  
)
if exist "%CARBON_HOME%\repository\components\dropins\asm_util_*.jar" (
  set SERVER_RESTART_REQUIRED="true"
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\dropins\asm_util_!LOCAL_ASM_VERSION!*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  echo Remove existing ASM Util library from dropins: !full_artifact_name!
  del !location!
  echo ASM Util library Removed from components\dropins.  
)
echo Adaptive authentication successfully disabled.
goto printRestartMsg


:enableAdaptiveAuth
if exist "%CARBON_HOME%\repository\components\lib\nashorn-core-*.jar" (
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\nashorn-core-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_NASHORN_VERSION=%%l.%%m"

  if %NASHORN_VERSION%==!LOCAL_NASHORN_VERSION!  (
    echo Nashorn library exists. No need to download.
  ) else (
    set SERVER_RESTART_REQUIRED="true"
    echo Required Nashorn library not found. Remove existing library : !full_artifact_name!
    del !location!
    if exist "%CARBON_HOME%\repository\components\dropins\nashorn_core_*.jar" (
       for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\dropins\nashorn_core_!LOCAL_NASHORN_VERSION!*.jar') do set "location=%%i"
       for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
       echo Remove existing Nashorn library from dropins: !full_artifact_name!
       del !location!
       echo Nashorn library Removed from components\dropins.  
    )     
    echo Downloading required Nashorn library : nashorn-core-%NASHORN_VERSION%
    call mvn dependency:get -Dartifact=org.openjdk.nashorn:nashorn-core:%NASHORN_VERSION% -Ddest=%LIB_REPO%
    echo Nashorn library updated.
  )
) else (
  set SERVER_RESTART_REQUIRED="true"
  echo Nashorn library not found. Starting to download.....
  call mvn dependency:get -Dartifact=org.openjdk.nashorn:nashorn-core:%NASHORN_VERSION% -Ddest=%LIB_REPO%
  echo Nashorn download completed. Downloaded version : nashorn-core-%NASHORN_VERSION%
)

if exist "%CARBON_HOME%\repository\components\lib\asm-util-*.jar" (
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\asm-util-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"

  if %ASM_VERSION%==!LOCAL_ASM_VERSION!  (
    echo ASM-Util library exists. No need to download.
  ) else (
    set SERVER_RESTART_REQUIRED="true"
    echo Required ASM-Util library not found. Remove existing library : !full_artifact_name!
    del !location!
    if exist "%CARBON_HOME%\repository\components\dropins\asm_util_*.jar" (
       for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\dropins\asm_util_!LOCAL_ASM_VERSION!*.jar') do set "location=%%i"
       for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
       echo Remove existing ASM Util library from dropins: !full_artifact_name!
       del !location!
       echo ASM Util library Removed from components\dropins.  
    )     
    echo Downloading required ASM-Util library : asm-util-%ASM_VERSION%
    call mvn dependency:get -Dartifact=org.ow2.asm:asm-util:%ASM_VERSION% -Ddest=%LIB_REPO%
    echo ASM-Util library updated.
  )
) else (
  set SERVER_RESTART_REQUIRED="true"
  echo ASM-Util library not found. Starting to download.....
  call mvn dependency:get -Dartifact=org.ow2.asm:asm-util:%ASM_VERSION% -Ddest=%LIB_REPO%
  echo ASM-Util download completed. Downloaded version : asm-util-%ASM_VERSION%
)
echo Adaptive authentication successfully enabled.
goto printRestartMsg

:printRestartMsg
if %SERVER_RESTART_REQUIRED%=="true" (
  echo Please restart the server.
)
goto end

:end
endlocal

