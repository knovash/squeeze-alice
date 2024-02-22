#!/usr/bin/env bash

echo "http://192.168.1.110:9000/plugins/spotty/index.html?index=10.1&player="$1"&sess="
curl "http://192.168.1.110:9000/plugins/spotty/index.html?index=10.1&player="$1"&sess="

#curl "http://192.168.1.110:9000/plugins/spotty/index.html?index=10.1&player=aa%3Aaa%3A96%3A95%3A81%3A94&sess="
