#!/usr/bin/env bash
# sudo chmod +x *.sh
#----------------------------------------------------------


echo -e ${BGreen}"MVN PACKAGE"${NC}
rm -r target
mvn package


echo -e ${BGreen}"MVN FINISH"${NC}
sleep 60
#$SHELL
