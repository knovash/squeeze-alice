#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"READ FROM set_lms_ip.txt"${NC}
remote=`cat set_lms_ip.txt`
echo -e ${BGreen}"LMS IP "$remote${NC}

ssh root@$remote

#$SHELL
