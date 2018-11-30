echo "-----------------------------------------------------------------------"
echo "|                                                                     |"
echo "| * This script will download a tomcat distribution and deploy        |"
echo "|  following sample applications.                                     |"
echo "|     -travelocity.com                                                |"
echo "|                                                                     |"
echo "| * Before proceeding sure you have installed 'wget'.                 |"
echo "|     Ex: for MacOS to install 'wget', use below command              |"
echo "|          brew install wget                                          |"
echo "|                                                                     |"
echo "| * Make sure other instances of tomcat is not running in this        |"
echo "|    in default port: 8080                                            |"
echo "|                                                                     |"
echo "| * sh deploy-samples-mac.sh <ISHttpsUrl>                             |"
echo "|                                                                     |"
echo "-----------------------------------------------------------------------"
ISHttpsUrl="$1"
echo "ISHttpsURL is set to: $ISHttpsUrl"


mkdir target
cd target

wget https://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.35/bin/apache-tomcat-8.5.35.zip
unzip apache-tomcat-8.5.35.zip

wget http://maven.wso2.org/nexus/content/repositories/releases/org/wso2/is/org.wso2.sample.is.sso.agent/5.7.0/org.wso2.sample.is.sso.agent-5.7.0.war
unzip org.wso2.sample.is.sso.agent-5.7.0.war -d travelocity.com
mv travelocity.com apache-tomcat-8.5.35/webapps/

cd apache-tomcat-8.5.35/

if [ $# -eq 0 ]
  then
    echo "ISHttpsUrl is not provided, hence using default configurations."
  else
    sed -i.bak 's,https://localhost:9443,'$(echo ${ISHttpsUrl})',g' webapps/travelocity.com/WEB-INF/classes/travelocity.properties
    sed -i.bak 's,SAML2.IdPEntityId=localhost,SAML2.IdPEntityId='$(echo ${ISHttpsUrl} | sed 's,https://,,g' | sed 's,:9443,,g' | sed 's,:443,,g')',g' webapps/travelocity.com/WEB-INF/classes/travelocity.properties
    #sed -i.bak 's,http://localhost:8080,http://${TomcatHost}:8080,g' webapps/travelocity.com/WEB-INF/classes/travelocity.properties
fi

cd bin
sh catalina.sh start

echo "Tomcat server is started on http://localhost:8080"