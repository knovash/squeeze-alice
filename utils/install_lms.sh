#!/usr/bin/env bash
# sudo chmod +x *.sh

echo ""
echo "START INSTALL LMS"

set=`cat set.json`
remote_ip=$(echo $set | jq .remote_ip)
echo $remote_ip

#echo "INSTALL PERL"
cd /root
sudo apt-get install libio-socket-ssl-perl
sudo apt-get install libcrypt-openssl-rsa-perl
echo "DOWNLOAD LMS"
rm lyr*
wget https://downloads.lms-community.org/nightly/lyrionmusicserver_9.0.0~1723965286_arm.deb
echo "INSTALL LMS"
sudo dpkg -i lyrionmusicser*.deb

sshpass -p "12345" scp /home/konstantin/Downloads/LMS/*.opml root@$remote_ip:/var/lib/squeezeboxserver/prefs/

#echo "if error try sudo apt --fix-broken install"
#echo "sudo nano /lib/systemd/system/logitechmediaserver.service"
#echo "Restart=always"

echo "FINISH INSTALL LMS
