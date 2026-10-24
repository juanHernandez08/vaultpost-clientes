#!/usr/bin/env bash
# Compila y despliega el módulo en un Tomcat 10 local sin Maven (uso en el entorno de pruebas)
set -e
B=/tmp/war; TB=/tmp/tbase
CP=/usr/share/java/mariadb-java-client.jar:/usr/share/tomcat10/lib/servlet-api.jar:/usr/share/tomcat10/lib/jsp-api.jar
rm -rf $B && mkdir -p $B/WEB-INF/classes $B/WEB-INF/lib
javac -encoding UTF-8 --release 17 -d $B/WEB-INF/classes -cp $CP $(find src/main/java -name "*.java")
cp src/main/resources/db.properties $B/WEB-INF/classes/
cp -r src/main/webapp/* $B/
cp /usr/share/java/mariadb-java-client.jar $B/WEB-INF/lib/
export CATALINA_HOME=/usr/share/tomcat10 CATALINA_BASE=$TB
$CATALINA_HOME/bin/catalina.sh stop >/dev/null 2>&1 || true; sleep 3
rm -rf $TB/webapps/vaultpost-clientes $TB/work/* && mkdir -p $TB/webapps/vaultpost-clientes && cp -r $B/* $TB/webapps/vaultpost-clientes/
$CATALINA_HOME/bin/catalina.sh start >/dev/null 2>&1
