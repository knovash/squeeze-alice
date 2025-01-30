#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"VPN FRANCE"${NC}

sudo openvpn --config France-tcp.ovpn --auth-user-pass pass_fran.txt


#sleep 10
$SHELL
