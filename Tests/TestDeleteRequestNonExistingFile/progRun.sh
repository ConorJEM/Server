#!/bin/bash
ROOT=../rrr
PORT=$((12000 + ($RANDOM % 1000)))
(timeout 2 java WebServerMain $ROOT $PORT > /dev/null 2>&1 ) & (sleep 1 ; curl -s -I -X DELETE localhost:$PORT/file1.txt | grep -i -a 'HTTP/1.1')
wait
