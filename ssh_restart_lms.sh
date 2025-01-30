#!/usr/bin/env bash
# sudo chmod +x *.sh

echo -e ${BGreen}"READ FROM set_lms_ip.txt"${NC}
remote=`cat set_lms_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}
# $remote

#ssh root@$remote systemctl daemon-reload
#ssh root@$remote systemctl restart logitechmediaserver.service
ssh root@$remote systemctl restart lyrionmusicserver.service 

sleep 10
