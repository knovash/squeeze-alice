#!/usr/bin/env bash
rm -r target
mvn package
./pack.sh
$SHELL
