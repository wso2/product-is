@echo off
rem ----------------------------------------------------------------------------
rem Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

set BC_FIPS_VERSION=1.0.2.3
set BCPKIX_FIPS_VERSION=1.0.7
set BCPROV_JDK15ON_VERSION=1.70.0.wso2v1
set BCPKIX_JDK15ON_VERSION=1.70.0.wso2v1

rem ----- Only set CARBON_HOME if not already set ----------------------------
:checkServer
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%CARBON_HOME%"=="" set CARBON_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET wsasDrive=%CARBON_HOME:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:
echo  "%CARBON_HOME%"

rem find CARBON_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if not exist "%CARBON_HOME%\bin\version.txt" goto noServerHome

set DISABLE=%1
set bundles_info=%CARBON_HOME%\repository\components\default\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
set bcprov_text=bcprov-jdk15on,%BCPKIX_JDK15ON_VERSION%,../plugins/bcprov-jdk15on_%BCPKIX_JDK15ON_VERSION%.jar,4,true
set bcpkix_text=bcpkix-jdk15on,%BCPROV_JDK15ON_VERSION%,../plugins/bcpkix-jdk15on_%BCPROV_JDK15ON_VERSION%.jar,4,true
echo %bundles_info%

rem commandline arguement 'DISABLE' or 'disable' is passed
if "%DISABLE%"=="DISABLE" goto disableFipsMode
if "%DISABLE%"=="disable" goto disableFipsMode
rem no commandline arguements are passed
goto enableFipsMode

:disableFipsMode
if exist "%CARBON_HOME%\repository\components\lib\bc-fips*.jar" (
  echo Remove existing bc-fips_%BC_FIPS_VERSION% jar from lib folder.
  DEL /F "%CARBON_HOME%\repository\components\lib\bc-fips*.jar"
  echo Bc-fips jar Removed from components\lib.
)
if exist "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar" (
  echo Remove existing bcPKIX-fips_%BC_FIPS_VERSION% jar from lib folder.
  DEL /F "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar"
  echo Bcpkix-fips jar Removed from components\lib.
)
if exist "%CARBON_HOME%\repository\components\dropins\bc_fips*.jar" (
  echo Remove existing bc_fips_%BC_FIPS_VERSION% jar from dropins folder.
  DEL /F "%CARBON_HOME%\repository\components\dropins\bc_fips*.jar"
  echo Bc-fips jar Removed from components\dropins.
)
if exist "%CARBON_HOME%\repository\components\dropins\bcpkix_fips*.jar" (
  echo Remove existing bc_fips_%BC_FIPS_VERSION% jar from dropins folder.
  DEL /F "%CARBON_HOME%\repository\components\dropins\bcpkix_fips*.jar"
  echo Bc-fips jar Removed from components\dropins.
)
if not exist "%CARBON_HOME%\repository\components\plugins\bcprov-jdk15on*.jar" (
	echo Downloading required bcprov-jdk15on jar : bcprov-jdk15on-%BCPROV_JDK15ON_VERSION%
	curl https://maven.wso2.org/nexus/content/repositories/releases/org/wso2/orbit/org/bouncycastle/bcprov-jdk15on/%BCPROV_JDK15ON_VERSION%/bcprov-jdk15on-%BCPROV_JDK15ON_VERSION%.jar -o %CARBON_HOME%/repository/components/plugins/bcprov-jdk15on_%BCPROV_JDK15ON_VERSION%.jar
)
if not exist "%CARBON_HOME%\repository\components\plugins\bcpkix-jdk15on*.jar" (
	echo Downloading required bcpkix-jdk15on jar : bcpkix-jdk15on-%BCPKIX_JDK15ON_VERSION%
	curl https://maven.wso2.org/nexus/content/repositories/releases/org/wso2/orbit/org/bouncycastle/bcpkix-jdk15on/%BCPKIX_JDK15ON_VERSION%/bcpkix-jdk15on-%BCPKIX_JDK15ON_VERSION%.jar -o %CARBON_HOME%/repository/components/plugins/bcpkix-jdk15on_%BCPKIX_JDK15ON_VERSION%.jar
)
findstr /c:%bcprov_text% %bundles_info% > nul
if %errorlevel%==1 (
    echo %bcprov_text% >>  %bundles_info%
)
findstr /c:%bcpkix_text% %bundles_info% > nul
if %errorlevel%==1 (
    echo %bcpkix_text% >>  %bundles_info%
)
goto printRestartMsg

: enableFipsMode

if exist "%CARBON_HOME%\repository\components\plugins\bcprov-jdk15on*" (
  echo Remove existing bcprov-jdk15on jar from plugins folder.
  DEL /F "%CARBON_HOME%\repository\components\plugins\bcprov-jdk15on*"
  echo bcprov-jdk15on jar Removed from components\plugins.
)
if exist "%CARBON_HOME%\repository\components\plugins\bcpkix-jdk15on*" (
  echo Remove existing bcpkix-jdk15on jar from plugins folder.
  DEL /F "%CARBON_HOME%\repository\components\plugins\bcpkix-jdk15on*"
  echo bcpkix-jdk15on jar Removed from components\plugins.
)
if not exist "%CARBON_HOME%\repository\components\lib\bc-fips*.jar" (
	echo Downloading required bc-fips jar : bc-fips-%BC_FIPS_VERSION%
	curl https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/%BC_FIPS_VERSION%/bc-fips-%BC_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bc-fips-%BC_FIPS_VERSION%.jar
)
if not exist "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar" (
	echo Downloading required bcpkix-fips jar : bcpkix-fips-%BCPKIX_FIPS_VERSION%
	curl https://repo1.maven.org/maven2/org/bouncycastle/bcpkix-fips/%BCPKIX_FIPS_VERSION%/bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar
)
set temp_file=%CARBON_HOME%\repository\components\default\configuration\org.eclipse.equinox.simpleconfigurator\temp.info
findstr /v /c:%bcprov_text% /c:%bcpkix_text% !bundles_info! > !temp_file!
move /y !temp_file! !bundles_info!
goto printRestartMsg

:printRestartMsg
echo Please restart the server.
goto end

:noServerHome
echo CARBON_HOME is set incorrectly or CARBON could not be located. Please set CARBON_HOME.
goto end

:end
endlocal
