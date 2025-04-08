#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color
echo -e ${BGreen}"START"${NC}

#rm -r target
#mvn package
sudo mkdir -p /opt/squeeze-alice-1.0

echo -e ${BGreen}"COPY JAR squeeze-alice-1.0.jar TO /opt/squeeze-alice-1.0/"${NC}
sudo cp target/*.jar /opt/squeeze-alice-1.0/

echo -e ${BGreen}"COPY SERVICE squeeze-alice.service TO /lib/systemd/system/"${NC}
sudo cp utils/squeeze-alice.service /lib/systemd/system/

echo -e ${BGreen}"SYSTEMCTL ENABLE, RELOAD, RESTART"${NC}
sudo systemctl enable squeeze-alice.service
sudo systemctl daemon-reload
sudo systemctl restart squeeze-alice.service
echo -e ${BGreen}"FINISH"${NC}

echo -e "\n"${BGreen}"http://"$remote":8010"${NC}

#sleep 10
echo -e ${BGreen}"\nSTATUS"${NC}
sudo systemctl status squeeze-alice.service
echo -e ${BGreen}"\nLOG"${NC}
tail -f /opt/squeeze-alice-1.0/data/log.txt

#sleep 60
$SHELL
