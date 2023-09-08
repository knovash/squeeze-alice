#!/usr/bin/env bash
rm -r squeeze-alice-pak.tar.gz
cd squeeze-alice-pak
rm -r squeeze-alice
mkdir squeeze-alice
cp -r ../target/* squeeze-alice/
tar -czvf squeeze-alice-1.0.tar.gz squeeze-alice
rm -r squeeze-alice
cd ..
rm -r squeeze-alice-pak.tar.gz
tar -czvf squeeze-alice-pak.tar.gz squeeze-alice-pak
$SHELL
