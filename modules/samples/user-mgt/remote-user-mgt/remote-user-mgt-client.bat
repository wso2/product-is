@echo off

:checkJava
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto runClient

:noJavaHome
echo "You must set the JAVA_HOME variable before running the client"
goto end

:runClient
set CLASSPATH=target\*;target\classes\lib\*
"%JAVA_HOME%\bin\java" -classpath %CLASSPATH% org.wso2.remoteum.sample.RemoteUMClient

:end

:END
