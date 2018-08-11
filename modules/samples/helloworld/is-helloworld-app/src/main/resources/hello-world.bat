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
@echo off

:checkJava
    if "%JAVA_HOME%" == "" goto :noJavaHome
    if not exist "%JAVA_HOME%\bin\java.exe" goto :noJavaHome
    goto :okJavaHome

:noJavaHome
    echo "You must set the JAVA_HOME variable before running the client"
    goto :end

:okJavaHome
    REM check the CARBON_HOME environment variable

    set CURRENT_DIR=%cd%
    if not "%CARBON_HOME%" == "" goto :gotHome
    set CARBON_HOME=%CURRENT_DIR%
    if exist "%CARBON_HOME%\bin\hello-world.bat" goto :init

    REM guess the home. Jump one directory up to check if that is the home
    cd ..
    set CARBON_HOME=%cd%
    cd %CARBON_HOME%

:gotHome
    if exist "%CARBON_HOME%\bin\hello-world.bat" goto :init

    REM set CARBON_HOME=%~sdp0..
    set CARBON_HOME=%~sdp0..
    if exist "%CARBON_HOME%\bin\hello-world.bat" goto :init

    echo The CARBON_HOME environment variable is not defined correctly
    echo This environment variable is needed to run this program
    goto :end

:init
    set "IDP_HOST="
    set "IDP_PORT="
    set "APP_HOST="
    set "APP_PORT=9090"
    set "APP_CONTEXT="
    set "USERNAME="
    set "PASSWORD="

:parse
    if "%~1"=="" goto :start

    if "%~1"=="--help"     goto :usage
    if "%~1"=="-h"         goto :usage

    if "%~1"=="-H"          set "IDP_HOST=%~2"      & shift & shift & goto :parse
    if "%~1"=="--idphost"   set "IDP_HOST=%~2"      & shift & shift & goto :parse

    if "%~1"=="-P"          set "IDP_PORT=%~2"      & shift & shift & goto :parse
    if "%~1"=="--idpport"   set "IDP_PORT=%~2"      & shift & shift & goto :parse

    if "%~1"=="-a"          set "APP_HOST=%~2"      & shift & shift & goto :parse
    if "%~1"=="--apphost"   set "APP_HOST=%~2"      & shift & shift & goto :parse

    if "%~1"=="-p"           set "APP_PORT=%~2"     & shift & shift & goto :parse

    if "%~1"=="-c"           set "APP_CONTEXT=%~2"  & shift & shift & goto :parse
    if "%~1"=="--appcontext" set "APP_CONTEXT=%~2"  & shift & shift & goto :parse

    if "%~1"=="-u"           set "USERNAME=%~2"     & shift & shift & goto :parse
    if "%~1"=="--username"   set "USERNAME=%~2"     & shift & shift & goto :parse

    if "%~1"=="-s"           set "PASSWORD=%~2"     & shift & shift & goto :parse
    if "%~1"=="--password"   set "PASSWORD=%~2"     & shift & shift & goto :parse

    shift
    goto :parse

:usage
    echo.
    echo Hello World is a simple OAuth app that can be used to quickly test single sign-on.
    echo.
    echo Usage: sh hello-world.sh [OPTIONS]
    echo.
    echo Available Options:
    echo.
    echo    -h, --help       Display this help and exit.
    echo    -H, --idphost    Hostname of the Identity Server. Default: localhost
    echo    -P, --idpport    Port of the Identity Server. Default: 9443
    echo    -a, --apphost    Hostname of the application. Set this if the application is
    echo                     accessed through a hostname other than localhost. This will be used to
    echo                     construct the redirect URI for the application
    echo    -p, --appport    Port which the application should start in. Default: 9090
    echo    -c, --appcontext URL context of the application. Use this to add a context to application URL. Default: /
    echo                     e.g: If the context is given as /hello, the appliction will be as
    http://localhost:9090/hello/
    echo    -u, --username   Admin username of Identity Server. This will be used to authenticate with APIs. Default: admin
    echo    -p, --password   Password of admin user. This will be used to authenticate with APIs. Default: admin
    goto :end

:start
    java -Didp.hostname="%IDP_HOST%" -Didp.port="%IDP_PORT%" -Dapp.hostname="%APP_HOST%" -Dapp.port="%APP_PORT%" -Dapp.context="%APP_CONTEXT%" -Dusername="%USERNAME%" -Dpassword="%PASSWORD%" -Dapp.home="%APP_HOME%" -jar "%CARBON_HOME%\lib\jetty-runner.jar" --port %APP_PORT% "%CARBON_HOME%\webapp\hello-world.war"
    goto :end

:end
:END
