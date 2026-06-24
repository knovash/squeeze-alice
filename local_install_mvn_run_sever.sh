#!/usr/bin/env bash
# sudo chmod +x *.sh

echo -e "RUN LOCAL INSTALL"

echo -e "RUN MVN PACKAGE"

rm -r target
mvn package

cd target/
echo -e "RUN SERVER"
java -jar squeeze-alice-1.0.jar


sleep 120
#$SHELL
