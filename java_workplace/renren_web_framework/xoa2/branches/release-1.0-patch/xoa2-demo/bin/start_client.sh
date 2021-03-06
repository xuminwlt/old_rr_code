# !bin/bash

if [ -z "$JAVA_HOME" ]; then
  export JAVA_HOME=`/opt/j2sdk/`;
fi
#echo $JAVA_HOME

if [ -z "$XOAServer_HOME" ]; then
  export XOAServer_HOME=`cd ..; pwd`;
fi
#echo $XOAServer_HOME

if [ -z "$XOAServer_MAINCLASS" ]; then
  XOAServer_MAINCLASS=com.renren.xoa2.demo.twitter.TwitterClient
fi
#echo $XOAServer_MAINCLASS

XOAServer_LIB="$XOAServer_HOME/lib $XOAServer_HOME/lib $XOAServer_HOME/conf"
#echo $XOAServer_LIB

for jarDir in $XOAServer_LIB; do
        for jarFile in `ls $jarDir`; do
                CLASS_PATH=$jarDir/$jarFile:$CLASS_PATH
        done
done
CLASS_PATH=$XOAServer_HOME/classes:$CLASS_PATH
echo $CLASS_PATH

JAVA_OPTS="-Dlog4j.configuration=file:$XOAServer_HOME/classes/log4j.properties";

$JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASS_PATH $XOAServer_MAINCLASS $@
