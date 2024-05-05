#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

#echo -e "\nrun "${BGreen}"STOP TUNNEL SERVICE"${NC}"\n"
#systemctl stop squeeze-tunnel.service

#echo -e "\n"${BGreen}"REMOVE ON REMOTE /opt/squeeze-alice"${NC}"\n"
#systemctl status squeeze-alice.service
#ssh root@$remote rm -r /opt/squeeze-alice*

echo -e "\nrun "${BGreen}"STOP SERVER SERVICE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
systemctl stop squeeze-alice.service
sleep 1

echo -e "\n"${BGreen}"REMOVE /opt/squeeze-alice"${NC}"\n"
printf '%s\n' "${PWD##*/}"
rm -r /opt/squeeze-alice*

cd /root
printf '%s\n' "DIR: ${PWD##*/}"

                        

echo -e "\nrun "${BGreen}"UNTAR SERVER TO /OPT/"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
sudo tar xzvf squeeze-alice-1.0.tar.gz -C /opt/

echo -e "\nrun "${BGreen}"RESTART SERVER SERVICE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
systemctl enable squeeze-alice.service
systemctl daemon-reload
systemctl restart squeeze-alice.service
echo -e "\nrun "${BGreen}"\nINSTALL FINISH"${NC}"\n"

