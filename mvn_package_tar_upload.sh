#!/usr/bin/env bash
rm -r target
mvn package
./tar.sh
cd squeeze-alice-pak
sh ssh_upload_123.sh
$SHELL
