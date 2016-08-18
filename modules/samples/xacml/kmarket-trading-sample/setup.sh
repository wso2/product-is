SAMPLE_CLASSPATH=""
for f in lib/*.jar
do
  SAMPLE_CLASSPATH=$SAMPLE_CLASSPATH:$f
done
for h in target/*jar
do
  SAMPLE_CLASSPATH=$SAMPLE_CLASSPATH:$h
done
SAMPLE_CLASSPATH=$SAMPLE_CLASSPATH:$CLASSPATH

$JAVA_HOME/bin/java -classpath "$SAMPLE_CLASSPATH" org.wso2.carbon.identity.samples.entitlement.kmarket.trading.KMarketAccessControl setup



