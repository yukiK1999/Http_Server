#!/bin/bash

PORT=8080
set -e

echo 'Creating hosts file for virtual hosts'
rm -f hosts.txt
echo 'cs356.org' >> hosts.txt
echo 'meme.com' >> hosts.txt

echo 'Creating single file for basic test'
echo 'Your basic server 200 works!' > index.txt

echo 'Starting server...'
./start.sh &
PID=`echo $!`
sleep 2
echo ''


echo '** Verifying basic server'
curl -H "Host:" localhost:$PORT/index.txt

curl -s -v -H "Host:" localhost:$PORT/bad.txt 2>&1 | grep '404'
echo 'Your basic server 404 works!'

curl -s -v -X PUT --http1.0 localhost:$PORT/bad.txt 2>&1 | grep '400'
echo 'Your basic server 400 works!'

##########
# Virtual hosts

echo ''
echo 'Creating files for virtual hosts'
mkdir -p meme.com cs356.org
echo 'Host 1 works!' > ./meme.com/index.txt
echo 'Host 2 works!' > ./cs356.org/index.txt

echo '** Verifying virtual hosts'
curl -s -H "Host: meme.com" localhost:$PORT/index.txt | grep '1'
curl -s -H "Host: cs356.org" localhost:$PORT/index.txt | grep '2'

##########
# POST

echo ''
echo '** Verifying POST'
rm -f ./meme.com/newFile.txt
curl -s -H "Host: meme.com" -X POST --data 'Here is some new data!' localhost:$PORT/newFile.txt
curl -s -H "Host: meme.com" localhost:$PORT/newFile.txt
echo ''

##########
# Caching

echo ''
echo '** Verifying caching'
curl -v -s -H "Host: meme.com" -H "If-Modified-Since: Fri, 06 Sep 2010 17:25:35 GMT" localhost:$PORT/newFile.txt 2>&1 | grep '200'
curl -v -s -H "Host: meme.com" -H "If-Modified-Since: Fri, 06 Sep 2020 17:25:35 GMT" localhost:$PORT/newFile.txt 2>&1 | grep '304'

##########
# Multi-thread

echo ''
echo '** Multi-threading will be verified manually from the source code.'

echo ''
echo '!! Congrats! All the test cases worked!'
echo ''

echo 'Stopping server...'
pkill -f ^java

echo 'Cleaning up...'
rm -rf *.class hosts.txt index.txt cs356.org meme.com
