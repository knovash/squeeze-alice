#!/usr/bin/env bash

remote=`cat set.json`
echo $remote
echo $remote | jq .port
echo $remote | jq .lmsPort
sss=$(echo $remote | jq .lmsPort)
echo $sss

$SHELL
