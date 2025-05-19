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

set BC_FIPS_VERSION=2.1.0
set BCPKIX_FIPS_VERSION=2.1.9
set BCUTIL_FIPS_VERSION=2.1.4

set EXPECTED_BC_FIPS_CHECKSUM=c8df3d47f9854f3e9ca57e9fc862da18c9381fa9
set EXPECTED_BCPKIX_FIPS_CHECKSUM=722eaefa83fd8c53e1fc019bde25e353258ed22b
set EXPECTED_BCUTIL_FIPS_CHECKSUM=1d37b7a28560684f5b8e4fd65478c9130d4015d0

rem ----- Only set CARBON_HOME if not already set ----------------------------
:checkServer
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%CARBON_HOME%"=="" set CARBON_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET wsasDrive=%CARBON_HOME:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:

rem find CARBON_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if not exist "%CARBON_HOME%\bin\version.txt" goto noServerHome

set ARGUEMENT=%1
set bundles_info=%CARBON_HOME%\repository\components\default\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
set "homeDir=%userprofile%"
set server_restart_required=false

rem commandline arguement 'DISABLE' or 'disable' is passed
if "%ARGUEMENT%"=="DISABLE" goto disableFipsMode
if "%ARGUEMENT%"=="disable" goto disableFipsMode
if "%ARGUEMENT%"=="VERIFY" goto verifyFipsMode
if "%ARGUEMENT%"=="verify" goto verifyFipsMode
rem no commandline arguements are passed
goto enableFipsMode

:disableFipsMode
if exist "%CARBON_HOME%\repository\components\lib\bc-fips*.jar" (
    set server_restart_required=true
    echo Remove existing bc-fips jar from lib folder.
    DEL /F "%CARBON_HOME%\repository\components\lib\bc-fips*.jar"
    echo Successfully removed bc-fips_%BC_FIPS_VERSION%.jar from components\lib.
)
if exist "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar" (
    set server_restart_required=true
    echo Remove existing bcpkix-fips jar from lib folder.
    DEL /F "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar"
    echo Successfully removed bcpkix-fips_%BCPKIX_FIPS_VERSION%.jar from components\lib.
)
if exist "%CARBON_HOME%\repository\components\lib\bcutil-fips*.jar" (
    set server_restart_required=true
    echo Remove existing bcutil-fips jar from lib folder.
    DEL /F "%CARBON_HOME%\repository\components\lib\bcutil-fips*.jar"
    echo Successfully removed bcutil-fips_%BCUTIL_FIPS_VERSION%.jar from components\lib.
)
if exist "%CARBON_HOME%\repository\components\dropins\bc_fips*.jar" (
    set server_restart_required=true
    echo Remove existing bc-fips jar from dropins folder.
    DEL /F "%CARBON_HOME%\repository\components\dropins\bc_fips*.jar"
    echo Successfully removed bc_fips_%BC_FIPS_VERSION%.jar from components\dropins.
)
if exist "%CARBON_HOME%\repository\components\dropins\bcpkix_fips*.jar" (
    set server_restart_required=true
    echo Remove existing bcpkix_fips jar from dropins folder.
    DEL /F "%CARBON_HOME%\repository\components\dropins\bcpkix_fips*.jar"
    echo Successfully removed bcpkix-fips_%BCPKIX_FIPS_VERSION%.jar from components\dropins.
)
if exist "%CARBON_HOME%\repository\components\dropins\bcutil_fips*.jar" (
    set server_restart_required=true
    echo Remove existing bcutil_fips jar from dropins folder.
    DEL /F "%CARBON_HOME%\repository\components\dropins\bcutil_fips*.jar"
    echo Successfully removed bcutil-fips_%BCUTIL_FIPS_VERSION%.jar from components\dropins.
)

if not exist "%CARBON_HOME%\repository\components\plugins\bcprov-jdk18on*.jar" (
    set server_restart_required=true
    if exist "%homeDir%\.wso2-bc\backup\bcprov-jdk18on*.jar" (
        for /r %homeDir%\.wso2-bc\backup\ %%G in (bcprov-jdk18on*.jar) do (
        set bcprov_location=%%G
        set file_name=%%~nG
	    goto checkbcprovVersion
    )
    :checkbcprovVersion
    for /f "tokens=2 delims=_" %%v in ("%bcprov_file_name%") do set "bcprov_version=%%v"
    goto bbb

    :bbb
    move "%bcprov_location%" "%CARBON_HOME%\repository\components\plugins"
    echo Moved %bcprov_file_name% from %homeDir%\.wso2-bc\backup to components/plugins.
    ) else ( echo "Required bcprov-jdk18on jar is not available in %homeDir%/.wso2-bc/backup. Download the jar from maven central repository." )
)

if not exist "%CARBON_HOME%\repository\components\plugins\bcpkix-jdk18on*.jar" (
    set server_restart_required=true
    if exist "%homeDir%\.wso2-bc\backup\bcpkix-jdk18on*.jar" (
        for /r %homeDir%\.wso2-bc\backup\ %%G in (bcpkix-jdk18on*.jar) do (
        set bcpkix_location=%%G
        set bcpkix_file_name=%%~nG
        set verify=false
	    goto foundBcPkix1
        )
    :foundBcPkix1
    for /f "tokens=2 delims=_" %%v in ("%bcpkix_file_name%") do set "bcpkix_version=%%v"
    goto bbb

    :bbb
    move "%bcpkix_location%" "%CARBON_HOME%\repository\components\plugins"
    echo Moved %bcpkix_file_name% from %homeDir%\.wso2-bc\backup to components/plugins.
    ) else ( echo "Required bcpkix-jdk18on jar is not available in %homeDir%/.wso2-bc/backup. Download the jar from maven central repository." )
)

findstr /c:%bcprov_text% %bundles_info% > nul
if %errorlevel%==1 (
    set server_restart_required=true
    echo %bcprov_text% >>  %bundles_info%
)
findstr /c:%bcpkix_text% %bundles_info% > nul
if %errorlevel%==1 (
    set server_restart_required=true
    echo %bcpkix_text% >>  %bundles_info%
)
goto printRestartMsg

:enableFipsMode
set arg1=
set arg2=
:parse_args
if "%~1" == "" goto :done_args
if /I "%~1" == "-f" set "arg1=%~2" & shift
if /I "%~1" == "-m" set "arg2=%~2" & shift
shift
goto :parse_args
:done_args

if not exist "%homeDir%\.wso2-bc" (
    mkdir "%homeDir%\.wso2-bc"
)
if not exist "%homeDir%\.wso2-bc\backup" (
    mkdir "%homeDir%\.wso2-bc\backup"
)

if exist %CARBON_HOME%\repository\components\plugins\bcprov-jdk18on*.jar (
    set server_restart_required=true
    for /r %CARBON_HOME%\repository\components\plugins\ %%G in (bcprov-jdk18on*.jar) do (
        set bcprov_location=%%G
        set bcprov_file_name=%%~nG
	goto checkBcVersion
    )
    :checkBcVersion
    for /f "tokens=2 delims=_" %%v in ("%bcprov_file_name%") do set "bcprov_version=%%v"
    goto removeBcProv

    :removeBcProv
    echo Remove existing bcprov-jdk18on jar from plugins folder.
    if exist "%homeDir%\.wso2-bc\backup\bcprov-jdk18on*.jar" (
        DEL /F "%homeDir%\.wso2-bc\backup\bcprov-jdk18on*.jar"
    )
    move "%bcprov_location%" "%homeDir%\.wso2-bc\backup"
    echo Successfully removed %bcprov_file_name% from components\plugins.
)

if exist %CARBON_HOME%\repository\components\plugins\bcpkix-jdk18on*.jar (
    set server_restart_required=true
    for /r %CARBON_HOME%\repository\components\plugins\ %%G in (bcpkix-jdk18on*.jar) do (
        set bcpkix_location=%%G
        set bcpkix_file_name=%%~nG
	goto checkBcpkixVersion
    )
    :checkBcpkixVersion
    for /f "tokens=2 delims=_" %%v in ("%bcpkix_file_name%") do set "bcpkix_version=%%v"
    goto removeBcPkix

    :removeBcPkix
   	echo Remove existing bcpkix-jdk18on jar from plugins folder.
   	if exist "%homeDir%\.wso2-bc\backup\bcpkix-jdk18on*.jar" (
        DEL /F "%homeDir%\.wso2-bc\backup\bcpkix-jdk18on*.jar"
    )
    move "%bcpkix_location%" "%homeDir%\.wso2-bc\backup"
    echo Successfully removed %bcpkix_file_name% from components\plugins.
)

if exist "%CARBON_HOME%\repository\components\lib\bc-fips*.jar" (
    for /f "delims=" %%a in ('dir /b /s "%CARBON_HOME%\repository\components\lib\bc-fips*.jar"') do (
        set bcfips_location=%%a
        goto check_bcfips_location
    )
    :check_bcfips_location
    for %%f in ("%bcfips_location%") do set "bcfips_location=%%~nxf"
    if not "%bcfips_location%"=="bc-fips-%BC_FIPS_VERSION%.jar" (
        set sever_restart_required=true
        echo There is an update for bc-fips. Therefore Remove existing bc-fips jar from lib folder.
        del /q "%CARBON_HOME%\repository\components\lib\bc-fips*.jar" 2> nul
        echo Successfully removed bc-fips_%BC_FIPS_VERSION%.jar from components/lib.
        if exist "%CARBON_HOME%\repository\components\dropins\bc_fips*.jar" (
            set sever_restart_required=true
            echo Remove existing bc-fips jar from dropins folder.
            del /q "%CARBON_HOME%\repository\components\dropins\bc_fips*.jar" 2> nul
            echo Successfully removed bc-fips_%BC_FIPS_VERSION%.jar from components/dropins.
        )
    )
)

if exist "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar" (
    for /f "delims=" %%a in ('dir /b /s "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar"') do (
        set bcpkixfips_location=%%a
        goto check_bcpkixfips_location
    )
    :check_bcpkixfips_location
    for %%f in ("%bcpkixfips_location%") do set "bcpkixfips_location=%%~nxf"
    if not "%bcpkixfips_location%"=="bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar" (
        set sever_restart_required=true
        echo There is an update for bcpkix-fips. Therefore Remove existing bcpkix-fips jar from lib folder.
        del /q "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar" 2> nul
        echo Successfully removed bcpkix-fips_%BCPKIX_FIPS_VERSION%.jar from components/lib.
        if exist "%CARBON_HOME%\repository\components\dropins\bcpkix_fips*.jar" (
            set sever_restart_required=true
            echo Remove existing bcpkix-fips jar from dropins folder.
            del /q "%CARBON_HOME%\repository\components\dropins\bcpkix_fips*.jar" 2> nul
            echo Successfully removed bcpkix-fips_%BCPKIX_FIPS_VERSION%.jar from components/dropins.
        )
    )
)

if exist "%CARBON_HOME%\repository\components\lib\bcutil-fips*.jar" (
    for /f "delims=" %%a in ('dir /b /s "%CARBON_HOME%\repository\components\lib\bcutil-fips*.jar"') do (
        set bcutilfips_location=%%a
        goto check_bcutilfips_location
    )
    :check_bcutilfips_location
    for %%f in ("%bcutilfips_location%") do set "bcutilfips_location=%%~nxf"
    if not "%bcutilfips_location%"=="bcutil-fips-%BCUTIL_FIPS_VERSION%.jar" (
        set sever_restart_required=true
        echo There is an update for bcutil-fips. Therefore Remove existing bcutil-fips jar from lib folder.
        del /q "%CARBON_HOME%\repository\components\lib\bcutil-fips*.jar" 2> nul
        echo Successfully removed bcutil-fips_%BCUTIL_FIPS_VERSION%.jar from components/lib.
        if exist "%CARBON_HOME%\repository\components\dropins\bcutil_fips*.jar" (
            set sever_restart_required=true
            echo Remove existing bcutil-fips jar from dropins folder.
            del /q "%CARBON_HOME%\repository\components\dropins\bcutil_fips*.jar" 2> nul
            echo Successfully removed bcutil-fips_%BCUTIL_FIPS_VERSION%.jar from components/dropins.
        )
    )
)

if not exist "%CARBON_HOME%\repository\components\lib\bc-fips*.jar" (
    set server_restart_required=true
	if not "%arg1%"=="" (
	    if not exist "%arg1%\bc-fips-%BC_FIPS_VERSION%.jar" (
	    	echo Can not be found requried bc-fips-%BC_FIPS_VERSION%.jar in given file path : "%arg1%".
	    ) else (
		    copy "%arg1%\bc-fips-%BC_FIPS_VERSION%.jar" "%CARBON_HOME%\repository\components\lib\"
            if %errorlevel% equ 0 (
                echo bc-fips JAR file copied successfully.
            ) else (
                echo Error copying bc-fips JAR file.
            )
    )
	)
	if not "%arg2%"=="" if "%arg1%"=="" (
        echo Downloading required bc-fips jar : bc-fips-%BC_FIPS_VERSION%
	    curl %arg2%/org/bouncycastle/bc-fips/%BC_FIPS_VERSION%/bc-fips-%BC_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bc-fips-%BC_FIPS_VERSION%.jar
        FOR /F "tokens=*" %%G IN ('certutil -hashfile "%CARBON_HOME%\repository\components\lib\bc-fips-%BC_FIPS_VERSION%.jar" SHA1 ^| FIND /V ":"') DO SET "ACTUAL_CHECKSUM_BC_FIPS=%%G"
        if "%ACTUAL_CHECKSUM_BC_FIPS%"=="%EXPECTED_BC_FIPS_CHECKSUM%" (
            echo Checksum verified: The downloaded bc-fips-%BC_FIPS_VERSION%.jar is valid.
        ) else (
            echo Checksum verification failed: The downloaded bc-fips-%BC_FIPS_VERSION%.jar may be corrupted.
        )
	)
	if "%arg1%"=="" if "%arg2%"=="" (
	    echo Downloading required bc-fips jar : bc-fips-%BC_FIPS_VERSION%
	    curl https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/%BC_FIPS_VERSION%/bc-fips-%BC_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bc-fips-%BC_FIPS_VERSION%.jar
        FOR /F "tokens=*" %%G IN ('certutil -hashfile "%CARBON_HOME%\repository\components\lib\bc-fips-%BC_FIPS_VERSION%.jar" SHA1 ^| FIND /V ":"') DO SET "ACTUAL_CHECKSUM_BC_FIPS=%%G"
        if "%ACTUAL_CHECKSUM_BC_FIPS%"=="%EXPECTED_BC_FIPS_CHECKSUM%" (
            echo Checksum verified: The downloaded bc-fips-%BC_FIPS_VERSION%.jar is valid.
        ) else (
            echo Checksum verification failed: The downloaded bc-fips-%BC_FIPS_VERSION%.jar may be corrupted.
        )
    )
)

if not exist "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar" (
    set server_restart_required=true
	if not "%arg1%"=="" (
	if not exist "%arg1%\bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar" (
		echo Can not be found requried bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar in given file path : "%arg1%".
	) else (
        copy "%arg1%\bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar" "%CARBON_HOME%\repository\components\lib\"
        if %errorlevel% equ 0 (
            echo bcpkix-fips JAR file copied successfully.
        ) else (
            echo Error copying bcpkix-fips JAR file.
        )
	)
	)
	if not "%arg2%"=="" if "%arg1%"=="" (
        echo Downloading required bcpkix-fips jar : bcpkix-fips-%BCPKIX_FIPS_VERSION%
	    curl %arg2%/org/bouncycastle/bcpkix-fips/%BCPKIX_FIPS_VERSION%/bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar
        FOR /F "tokens=*" %%G IN ('certutil -hashfile "%CARBON_HOME%\repository\components\lib\bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar" SHA1 ^| FIND /V ":"') DO SET "ACTUAL_CHECKSUM_BCPKIX_FIPS=%%G"
        if "%ACTUAL_CHECKSUM_BCPKIX_FIPS%"=="%EXPECTED_BCPKIX_FIPS_CHECKSUM%" (
            echo Checksum verified: The downloaded bcpkix-%BCPKIX_FIPS_VERSION%.jar is valid.
        ) else (
            echo Checksum verification failed: The downloaded bcpkix-%BCPKIX_FIPS_VERSION%.jar may be corrupted.
        )
	)
	if "%arg1%"=="" if "%arg2%"=="" (
	    echo Downloading required bcpkix-fips jar : bcpkix-fips-%BCPKIX_FIPS_VERSION%
	    curl https://repo1.maven.org/maven2/org/bouncycastle/bcpkix-fips/%BCPKIX_FIPS_VERSION%/bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar
        FOR /F "tokens=*" %%G IN ('certutil -hashfile "%CARBON_HOME%\repository\components\lib\bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar" SHA1 ^| FIND /V ":"') DO SET "ACTUAL_CHECKSUM_BCPKIX_FIPS=%%G"
        if "%ACTUAL_CHECKSUM_BCPKIX_FIPS%"=="%EXPECTED_BCPKIX_FIPS_CHECKSUM%" (
            echo Checksum verified: The downloaded bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar is valid.
        ) else (
            echo Checksum verification failed: The downloaded bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar may be corrupted.
        )
    )
)

if not exist "%CARBON_HOME%\repository\components\lib\bcutil-fips*.jar" (
    set server_restart_required=true
    if not "%arg1%"=="" (
    if not exist "%arg1%\bcutil-fips-%BCUTIL_FIPS_VERSION%.jar" (
        echo Can not be found requried bcutil-fips-%BCUTIL_FIPS_VERSION%.jar in given file path : "%arg1%".
    ) else (
        copy "%arg1%\bcutil-fips-%BCUTIL_FIPS_VERSION%.jar" "%CARBON_HOME%\repository\components\lib\"
        if %errorlevel% equ 0 (
            echo bcutil-fips JAR file copied successfully.
        ) else (
            echo Error copying bcutil-fips JAR file.
        )
    )
    )
    if not "%arg2%"=="" if "%arg1%"=="" (
        echo Downloading required bcutil-fips jar : bcutil-fips-%BCUTIL_FIPS_VERSION%
        curl %arg2%/org/bouncycastle/bcutil-fips/%BCUTIL_FIPS_VERSION%/bcutil-fips-%BCUTIL_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bcutil-fips-%BCUTIL_FIPS_VERSION%.jar
        FOR /F "tokens=*" %%G IN ('certutil -hashfile "%CARBON_HOME%\repository\components\lib\bcutil-fips-%BCUTIL_FIPS_VERSION%.jar" SHA1 ^| FIND /V ":"') DO SET "ACTUAL_CHECKSUM_BCUTIL_FIPS=%%G"
        if "%ACTUAL_CHECKSUM_BCUTIL_FIPS%"=="%EXPECTED_BCUTIL_FIPS_CHECKSUM%" (
            echo Checksum verified: The downloaded bcutil-fips-%BCUTIL_FIPS_VERSION%.jar is valid.
        ) else (
            echo Checksum verification failed: The downloaded bcutil-fips-%BCUTIL_FIPS_VERSION%.jar may be corrupted.
        )
    )
    if "%arg1%"=="" if "%arg2%"=="" (
        echo Downloading required bcutil-fips jar : bcutil-fips-%BCUTIL_FIPS_VERSION%
        curl https://repo1.maven.org/maven2/org/bouncycastle/bcutil-fips/%BCUTIL_FIPS_VERSION%/bcutil-fips-%BCUTIL_FIPS_VERSION%.jar -o %CARBON_HOME%/repository/components/lib/bcutil-fips-%BCUTIL_FIPS_VERSION%.jar
        FOR /F "tokens=*" %%G IN ('certutil -hashfile "%CARBON_HOME%\repository\components\lib\bcutil-fips-%BCUTIL_FIPS_VERSION%.jar" SHA1 ^| FIND /V ":"') DO SET "ACTUAL_CHECKSUM_BCUTIL_FIPS=%%G"
        if "%ACTUAL_CHECKSUM_BCUTIL_FIPS%"=="%EXPECTED_BCUTIL_FIPS_CHECKSUM%" (
            echo Checksum verified: The downloaded bcutil-fips-%BCUTIL_FIPS_VERSION%.jar is valid.
        ) else (
            echo Checksum verification failed: The downloaded bcutil-fips-%BCUTIL_FIPS_VERSION%.jar may be corrupted.
        )
    )
)
set bcprov_text=bcprov-jdk18on,%bcprov_version%,../plugins/bcprov-jdk18on_%bcprov_version%.jar,4,true
set bcpkix_text=bcpkix-jdk18on,%bcpkix_version%,../plugins/bcpkix-jdk18on_%bcpkix_version%.jar,4,true

set temp_file=%CARBON_HOME%\repository\components\default\configuration\org.eclipse.equinox.simpleconfigurator\temp.info
findstr /v /c:%bcprov_text% /c:%bcpkix_text% %bundles_info% > !temp_file!
move /y !temp_file! %bundles_info% > nul
goto printRestartMsg

:verifyFipsMode
set verify=true
if exist %CARBON_HOME%\repository\components\plugins\bcprov-jdk18on*.jar (
    for /r %CARBON_HOME%\repository\components\plugins\ %%G in (bcprov-jdk18on*.jar) do (
        set bc_location=%%G
        set file_name=%%~nG
        set verify=false
	goto foundBcProv
    )
    :foundBcProv
    echo Found %file_name% in plugins folder. This jar should be removed.
)

if exist %CARBON_HOME%\repository\components\plugins\bcpkix-jdk18on*.jar (
    for /r %CARBON_HOME%\repository\components\plugins\ %%G in (bcpkix-jdk18on*.jar) do (
        set bcpkix_location=%%G
        set file_name=%%~nG
        set verify=false
	goto foundBcPkix
    )
    :foundBcPkix
    echo Found %file_name% in plugins folder. This jar should be removed.
)

if exist "%CARBON_HOME%\repository\components\lib\bc-fips*.jar" (
	if not exist "%CARBON_HOME%\repository\components\lib\bc-fips-%BC_FIPS_VERSION%.jar" (
		set verify=false
		echo There is an update for bc-fips. Run the script again to get updates.
    )
)  else (
    set verify=false
    echo can not be found bc-fips_%BC_FIPS_VERSION%.jar in components/lib folder. This jar should be added.
)

if exist "%CARBON_HOME%\repository\components\lib\bcpkix-fips*.jar" (
	if not exist "%CARBON_HOME%\repository\components\lib\bcpkix-fips-%BCPKIX_FIPS_VERSION%.jar" (
		set verify=false
		echo There is an update for bcpkix-fips. Run the script again to get updates.
    )
) else (
    set verify=false
    echo can not be found bc-fips_%BC_FIPS_VERSION%.jar in components/lib folder. This jar should be added.
)

if exist "%CARBON_HOME%\repository\components\lib\bcutil-fips*.jar" (
    if not exist "%CARBON_HOME%\repository\components\lib\bcutil-fips-%BCUTIL_FIPS_VERSION%.jar" (
        set verify=false
        echo There is an update for bcutil-fips. Run the script again to get updates.
    )
) else (
    set verify=false
    echo can not be found bcutil-fips_%BCUTIL_FIPS_VERSION%.jar in components/lib folder. This jar should be added.
)

findstr /i /c:"bcprov-jdk18on" "%bundles_info%" >nul
if %errorlevel%==0 (
	set verify=false
  	echo Found bcprov-jdk18on entry in bundles.info. This should be removed.
)

findstr /i /c:"bcpkix-jdk18on" "%bundles_info%" >nul
if %errorlevel%==0 (
	set verify=false
  	echo Found bcpkix-jdk18on entry in bundles.info. This should be removed.
)
if "%verify%"=="true" (
	echo Verified : Product is FIPS compliant.
) else (
	echo Verification failed : Product is not FIPS compliant.
)
goto end

:printRestartMsg
if "%server_restart_required%"=="true" (
    echo Please restart the server.
)

goto end

:noServerHome
echo CARBON_HOME is set incorrectly or CARBON could not be located. Please set CARBON_HOME.
goto end

:end
endlocal
