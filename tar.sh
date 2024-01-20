#!/usr/bin/env bash

rm -r squeeze-alice-pak
mkdir squeeze-alice-pak
mkdir squeeze-alice-pak/squeeze-alice-1.0

cp -r target/* squeeze-alice-pak/squeeze-alice-1.0/
cp utils/*.sh squeeze-alice-pak/
cp utils/*.html squeeze-alice-pak/
cp utils/*.service squeeze-alice-pak/

cd squeeze-alice-pak
tar -czvf squeeze-alice-1.0.tar.gz squeeze-alice-1.0
rm -r squeeze-alice-1.0

cd ..
tar -czvf squeeze-alice-pak.tar.gz squeeze-alice-pak
#rm -r squeeze-alice-pak
 
#$SHELL
