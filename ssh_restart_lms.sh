#!/usr/bin/env bash
# sudo chmod +x *.sh

echo -e ${BGreen}"READ FROM set_lms_ip.txt"${NC}
remote=`cat set_lms_ip.txt`
echo -e ${BGreen}"LMS IP "$lms${NC}
# $lms

#ssh root@lms systemctl daemon-reload
ssh root@lms systemctl restart logitechmediaserver.service
