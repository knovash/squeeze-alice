#!/bin/bash
rm -r target
mvn package
./tar.sh
#$SHELL
