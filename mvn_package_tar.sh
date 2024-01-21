#!/usr/bin/env bash
rm -r target
mvn package
sh tar.sh
$SHELL
