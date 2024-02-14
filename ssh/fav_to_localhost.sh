#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color
name=$(basename "$0")
name=${name/.sh/}
name=${name/fav_to_/}
echo -e "favorites to "${BGreen}$name${NC}"\n"
#sshpass -p "12345" scp *.opml root@$name:/
sudo cp *.opml /var/lib/squeezeboxserver/prefs/
#sshpass -p "12345" scp *.opml root@192.168.1.110:/var/lib/squeezeboxserver/prefs/
$SHELL
