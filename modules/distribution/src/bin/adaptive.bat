@echo off
rem ----------------------------------------------------------------------------
rem Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
rem
rem WSO2 LLC. licenses this file to you under the Apache License,
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
rem    CARBON_HOME       Home of WSO2 Carbon installation. If not set I will  try
rem                      to figure it out.
rem    NASHORN_VERSION   OpenJDK Nashorn Version        	
rem    ASM_VERSION       ASM Util, Commons, Tree Version.
rem    JMS_VERSION       Geronimo Spec JMS Version.
rem
rem -----------------------------------------------------------------------------

set NASHORN_VERSION=15.3
set ASM_VERSION=9.2
set JMS_VERSION=1.1.0.rc4-wso2v1

set SERVER_RESTART_REQUIRED="false"

set DISABLE=%1

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

rem commandline arguement 'DISABLE' or 'disable' is passed
if "%DISABLE%"=="DISABLE" goto disableAdaptiveAuth
if "%DISABLE%"=="disable" goto disableAdaptiveAuth
rem no commandline arguements are passed
goto enableAdaptiveAuth

:disableAdaptiveAuth
echo ^^!^^!^^!This command will remove required libraries for adaptive authentication.^^!^^!^^!
echo ^^!^^!^^!If you disable it Adaptive Authentication feature will be disabled from the whole system^^!^^!^^!
echo ^^!^^!^^!Existing applications created with Adaptive Scripts may not work as expected^^!^^!^^!
set /p DECISION=^^!^^!^^!Please confirm the action, Are you going to disable Adaptive authentication(y/n)?^^!^^!^^!

if "%DECISION%"=="Y" goto proceedDisableAdaptiveAuth
if "%DECISION%"=="y" goto proceedDisableAdaptiveAuth
goto abortDisableAdaptiveAuth

:proceedDisableAdaptiveAuth
set LOCAL_NASHORN_VERSION=""
set LOCAL_ASM_VERSION=""
set LOCAL_JMS_VERSION=""

call :removeLibrary "Nashorn", "lib", "%CARBON_HOME%\repository\components\lib\nashorn-core-*.jar"
call :removeLibrary "Nashorn", "dropins", "%CARBON_HOME%\repository\components\dropins\nashorn_core_!LOCAL_NASHORN_VERSION!*.jar"
call :removeLibrary "Geronimo Spec Jms", "lib", "%CARBON_HOME%\repository\components\lib\geronimo-spec-jms-*.jar"
call :removeLibrary "Geronimo Spec Jms", "dropins", "%CARBON_HOME%\repository\components\dropins\geronimo_spec_jms_*.jar"
call :removeLibrary "ASM Util", "lib", "%CARBON_HOME%\repository\components\lib\asm-util-*.jar"
call :removeLibrary "ASM Util", "dropins", "%CARBON_HOME%\repository\components\dropins\asm_util_!LOCAL_ASM_VERSION!*.jar"
call :removeLibrary "ASM Commons", "lib", "%CARBON_HOME%\repository\components\lib\asm-commons-*.jar"
call :removeLibrary "ASM Commons", "dropins", "%CARBON_HOME%\repository\components\dropins\asm_commons_!LOCAL_ASM_VERSION!*.jar"
call :removeLibrary "ASM Tree", "lib", "%CARBON_HOME%\repository\components\lib\asm-tree-*.jar"
call :removeLibrary "ASM Tree", "dropins", "%CARBON_HOME%\repository\components\dropins\asm_tree_!LOCAL_ASM_VERSION!*.jar"

echo Adaptive authentication successfully disabled.
goto printRestartMsg

rem function to remove a jar matching a given file path pattern.
:removeLibrary
rem should be one of 'Nashorn', 'ASM-Util', 'ASM-Commons', 'ASM-Tree' or 'Geronimo-Spec-Jms'.
set jar_name=%~1
rem should be one of 'lib' or 'dropins'.
set folder=%~2
rem file path pattern to be matched.
set file_pattern=%~3

if exist "%file_pattern%" (  
  set SERVER_RESTART_REQUIRED="true"
  rem assign the file path matching the file_pattern to the variable location
  for /f "delims=" %%i in ('dir /s /b "%file_pattern%"') do set "location=%%i"
  rem assign the name of the file (without the path) to the variable full_artifact_name
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj" 

  rem if we are deleting from lib, we need to find the local versions and set those (To use when deleting from dropins).    
  if "%folder%"=="lib" (  
    rem extracts the a.b.jar and sets to the variable artifact_name
    for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"  
    if "%jar_name%"=="Nashorn" (
      rem extracts the a.b (i.e version) and sets it as local version
      for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_NASHORN_VERSION=%%l.%%m"
    ) else if "%jar_name%"=="ASM Util" (
      rem extracts the a.b (i.e version) and sets it as local version
      for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"
    ) else if "%jar_name%"=="ASM Commons" (
      rem extracts the a.b (i.e version) and sets it as local version
      for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"
    ) else if "%jar_name%"=="ASM Tree" (
      rem extracts the a.b (i.e version) and sets it as local version
      for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"
    ) else if "%jar_name%"=="Geronimo Spec Jms" (
      rem extracts the a.b (i.e version) and sets it as local version
      for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_JMS_VERSION=%%l.%%m"
    )
  )

  echo Remove existing %jar_name% library from %folder%: !full_artifact_name!
  del !location!
  echo %jar_name% library Removed from components\%folder%.
)
exit /B 0

:abortDisableAdaptiveAuth
echo Disabling Adaptive authentication is terminated.
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
    call :removeLibrary "Nashorn", "dropins", "%CARBON_HOME%\repository\components\dropins\nashorn_core_!LOCAL_NASHORN_VERSION!*.jar" 
    echo Downloading required Nashorn library : nashorn-core-%NASHORN_VERSION%
	  curl https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/%NASHORN_VERSION%/nashorn-core-%NASHORN_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/nashorn-core-%NASHORN_VERSION%.jar
    rem if download failed
    if !ERRORLEVEL! neq 0 (
      echo Nashorn library update failed with exit code: !ERRORLEVEL!
      exit /b !ERRORLEVEL!
    )
    echo Nashorn library updated.
  )
) else (
  set SERVER_RESTART_REQUIRED="true"
  echo Nashorn library not found. Starting to download.....
  curl https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/%NASHORN_VERSION%/nashorn-core-%NASHORN_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/nashorn-core-%NASHORN_VERSION%.jar
  rem if download failed
  if !ERRORLEVEL! neq 0 (
  	echo Nashorn library download failed with exit code: !ERRORLEVEL!
  	exit /b !ERRORLEVEL!
  )
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
    call :removeLibrary "ASM Util", "dropins", "%CARBON_HOME%\repository\components\dropins\asm_util_!LOCAL_ASM_VERSION!*.jar" 
    echo Downloading required ASM-Util library : asm-util-%ASM_VERSION%
	  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-util/%ASM_VERSION%/asm-util-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-util-%ASM_VERSION%.jar
	rem if download failed
    if !ERRORLEVEL! neq 0 (
      echo ASM-Util library update failed with exit code: !ERRORLEVEL!
      exit /b !ERRORLEVEL!
    )
    echo ASM-Util library updated.
  )
) else (
  set SERVER_RESTART_REQUIRED="true"
  echo ASM-Util library not found. Starting to download.....
  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-util/%ASM_VERSION%/asm-util-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-util-%ASM_VERSION%.jar
  rem if download failed
  set exit_code_a=!ERRORLEVEL!
  if !ERRORLEVEL! neq 0 (
    echo ASM-Util library download failed with exit code: !ERRORLEVEL!
    exit /b !ERRORLEVEL!
  )
  echo ASM-Util download completed. Downloaded version : asm-util-%ASM_VERSION%%
)

if exist "%CARBON_HOME%\repository\components\lib\asm-commons-*.jar" (
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\asm-commons-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj"
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"

  if %ASM_VERSION%==!LOCAL_ASM_VERSION!  (
    echo ASM-Commons library exists. No need to download.
  ) else (
    set SERVER_RESTART_REQUIRED="true"
    echo Required ASM-Commons library not found. Remove existing library : !full_artifact_name!
    del !location!
    call :removeLibrary "ASM Commons", "dropins", "%CARBON_HOME%\repository\components\dropins\asm_commons_!LOCAL_ASM_VERSION!*.jar"
    echo Downloading required ASM-Commons library : asm-commons-%ASM_VERSION%
	  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/%ASM_VERSION%/asm-commons-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-commons-%ASM_VERSION%.jar
	rem if download failed
    if !ERRORLEVEL! neq 0 (
      echo ASM-Commons library update failed with exit code: !ERRORLEVEL!
      exit /b !ERRORLEVEL!
    )
    echo ASM-Commons library updated.
  )
) else (
  set SERVER_RESTART_REQUIRED="true"
  echo ASM-Commons library not found. Starting to download.....
  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/%ASM_VERSION%/asm-commons-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-commons-%ASM_VERSION%.jar
  rem if download failed
  set exit_code_a=!ERRORLEVEL!
  if !ERRORLEVEL! neq 0 (
    echo ASM-Commons library download failed with exit code: !ERRORLEVEL!
    exit /b !ERRORLEVEL!
  )
  echo ASM-Commons download completed. Downloaded version : asm-commons-%ASM_VERSION%%
)

if exist "%CARBON_HOME%\repository\components\lib\asm-tree-*.jar" (
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\asm-tree-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj"
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_ASM_VERSION=%%l.%%m"

  if %ASM_VERSION%==!LOCAL_ASM_VERSION!  (
    echo ASM-Tree library exists. No need to download.
  ) else (
    set SERVER_RESTART_REQUIRED="true"
    echo Required ASM-Tree library not found. Remove existing library : !full_artifact_name!
    del !location!
    call :removeLibrary "ASM Tree", "dropins", "%CARBON_HOME%\repository\components\dropins\asm_tree_!LOCAL_ASM_VERSION!*.jar"
    echo Downloading required ASM-Tree library : asm-tree-%ASM_VERSION%
	  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/%ASM_VERSION%/asm-tree-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-tree-%ASM_VERSION%.jar
	rem if download failed
    if !ERRORLEVEL! neq 0 (
      echo ASM-Tree library update failed with exit code: !ERRORLEVEL!
      exit /b !ERRORLEVEL!
    )
    echo ASM-Tree library updated.
  )
) else (
  set SERVER_RESTART_REQUIRED="true"
  echo ASM-Tree library not found. Starting to download.....
  curl https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/%ASM_VERSION%/asm-tree-%ASM_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/asm-tree-%ASM_VERSION%.jar
  rem if download failed
  set exit_code_a=!ERRORLEVEL!
  if !ERRORLEVEL! neq 0 (
    echo ASM-Tree library download failed with exit code: !ERRORLEVEL!
    exit /b !ERRORLEVEL!
  )
  echo ASM-Tree download completed. Downloaded version : asm-tree-%ASM_VERSION%%
)

if exist "%CARBON_HOME%\repository\components\lib\geronimo-spec-jms-*.jar" (
  for /f "delims=" %%i in ('dir /s /b %CARBON_HOME%\repository\components\lib\geronimo-spec-jms-*.jar') do set "location=%%i"
  for %%j in (!location!!) do set "full_artifact_name=%%~nxj"
  for /f "tokens=3 delims=-" %%k in ("!full_artifact_name!") do set "artifact_name=%%k"
  for /f "tokens=1,2 delims=." %%l in ("!artifact_name!") do set "LOCAL_JMS_VERSION=%%l.%%m"

  if %JMS_VERSION%==!LOCAL_JMS_VERSION!  (
    echo Geronimo-Spec-Jms library exists. No need to download.
  ) else (
    set SERVER_RESTART_REQUIRED="true"
    echo Required Geronimo-Spec-Jms library not found. Remove existing library : !full_artifact_name!
    del !location!
    call :removeLibrary "Geronimo Spec Jms", "dropins", "%CARBON_HOME%\repository\components\dropins\geronimo_spec_jms_!LOCAL_JMS_VERSION!*.jar"
    echo Downloading required Geronimo-Spec-Jms library : geronimo-spec-jms-%JMS_VERSION%
	  curl https://dist.wso2.org/maven2/geronimo-spec/wso2/geronimo-spec-jms/%JMS_VERSION%/geronimo-spec-jms-%JMS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/geronimo-spec-jms-%JMS_VERSION%.jar
	rem if download failed
    if !ERRORLEVEL! neq 0 (
      echo Geronimo-Spec-Jms library update failed with exit code: !ERRORLEVEL!
      exit /b !ERRORLEVEL!
    )
    echo Geronimo-Spec-Jms library updated.
  )
) else (
  set SERVER_RESTART_REQUIRED="true"
  echo Geronimo-Spec-Jms library not found. Starting to download.....
  curl https://dist.wso2.org/maven2/geronimo-spec/wso2/geronimo-spec-jms/%JMS_VERSION%/geronimo-spec-jms-%JMS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/geronimo-spec-jms-%JMS_VERSION%.jar
  rem if download failed
  set exit_code_a=!ERRORLEVEL!
  if !ERRORLEVEL! neq 0 (
    echo Geronimo-Spec-Jms library download failed with exit code: !ERRORLEVEL!
    exit /b !ERRORLEVEL!
  )
  echo Geronimo-Spec-Jms download completed. Downloaded version : geronimo-spec-jms-%JMS_VERSION%%
)
echo Adaptive authentication successfully enabled.
goto printRestartMsg

:printRestartMsg
if %SERVER_RESTART_REQUIRED%=="true" (
  echo Please restart the server.
)
goto end

:noServerHome
echo CARBON_HOME is set incorrectly or CARBON could not be located. Please set CARBON_HOME.
goto end

:end
endlocal
