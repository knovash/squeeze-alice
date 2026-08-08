# sudo chmod ugo+x install.sh
# ./install.sh

echo "FIRST RUN INSTALL"

apt update
apt install rsync -y
apt install default-jre -y



echo "OK"
sleep 10
# $SHELL
