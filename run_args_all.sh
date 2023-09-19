#!/usr/bin/env bash
cd target
java -jar squeeze-alice*.jar -lmsip 192.168.1.52 -lmsport 9000 -port 8005 -context cmd
#$SHELL
