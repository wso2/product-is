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

rem Environment Variable Prequisites

rem    CARBON_HOME   	Home of WSO2 Carbon installation. If not set I will  try
rem                    	to figure it out.
rem    NASHORN_VERSION   	OpenJDK Nashorn Version
                   	
rem    ASM_VERSION   	ASM Util, Commons Version.

rem -----------------------------------------------------------------------------

set NASHORN_VERSION=15.3
set ASM_VERSION=9.2

SETLOCAL ENABLEDELAYEDEXPANSION

rem ----- Only set CARBON_HOME if not already set ----------------------------
:checkServer
setlocal enabledelayedexpansion
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%CARBON_HOME%"=="" set CARBON_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET wsasDrive=%CARBON_HOME:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:

rem find CARBON_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if not exist "%CARBON_HOME%\bin\version.txt" goto noServerHome

IF EXIST "%CARBON_HOME%\repository\components\lib\nashorn-core-*.jar" (
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\nashorn-core-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_NASHORN_VERSION=%%l.%%m"

  IF %NASHORN_VERSION%==!LOCAL_NASHORN_VERSION!  (
    echo Nashorn library exists. No need to download.
  ) ELSE (
    echo Required Nashorn library not found. Remove existing library : !full_artifact_name!
    del !location!
    echo Downloading required Nashorn library : nashorn-core-%NASHORN_VERSION%
	curl https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/%NASHORN_VERSION%/nashorn-core-%NASHORN_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/nashorn-core-%NASHORN_VERSION%.jar
    echo Nashorn library updated.
  )
) ELSE (
  echo Nashorn library not found. Starting to download.....
  curl https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/%NASHORN_VERSION%/nashorn-core-%NASHORN_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/nashorn-core-%NASHORN_VERSION%.jar
  echo Nashorn download completed. Downloaded version : nashorn-core-%NASHORN_VERSION%
)

IF EXIST "%CARBON_HOME%\repository\components\lib\asm-util-*.jar" (
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\asm-util-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"

  IF %ASM_VERSION%==!LOCAL_ASM_VERSION!  (
    echo ASM-Util library exists. No need to download.
  ) ELSE (
    echo Required ASM-Util library not found. Remove existing library : !full_artifact_name!
    del !location!
    echo Downloading required ASM-Util library : asm-util-%ASM_VERSION%
	  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-util/%ASM_VERSION%/asm-util-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-util-%ASM_VERSION%.jar
    echo ASM-Util library updated.
  )
) ELSE (
  echo ASM-Util library not found. Starting to download.....
  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-util/%ASM_VERSION%/asm-util-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-util-%ASM_VERSION%.jar
  echo ASM-Util download completed. Downloaded version : asm-util-%ASM_VERSION%%
)
echo Enable Adaptive Script successfully finished.
goto end

:noServerHome
echo CARBON_HOME is set incorrectly or CARBON could not be located. Please set CARBON_HOME.
goto end

:end
ENDLOCAL

