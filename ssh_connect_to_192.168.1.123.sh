#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color
file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"SSH CONNECT TO IP: $remote"${NC}
#read -p "Enter SSH username. Press Enter for 'root': " username
username=${username:-root}
#echo "USERNAME: "$username
ssh $username@$remote
