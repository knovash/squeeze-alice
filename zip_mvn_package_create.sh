#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"CLEAR TARGET AND GZ"${NC}
rm -r target
rm *.gz

echo -e ${BGreen}"MVN PACKAGE"${NC}
mvn clean package

echo -e ${BGreen}"TAR"${NC}
#tar -czvf squeeze-alice-local.tar.gz target/*.jar utils/squeeze-alice.service *install*.sh
tar -czvf squeeze-alice-local.tar.gz \
  --transform 's,.*/,,S' \
  target/squeeze-alice-1.0.jar \
  utils/squeeze-alice.service \
  install.txt \
  install_local.sh \
  install_ssh_192.168.1.123.sh \
  uninstall_local.sh \
  uninstall_ssh_192.168.1.123.sh

echo -e ${BGreen}"FINISH"${NC}

sleep 15
