#!/bin/bash

AIS_2_MAP_DIR="/home/pi/ais/ais2map/";
LOG_FILE=/dev/null;

echo "Start AIS 2 MAP..."; 
cd ${AIS_2_MAP_DIR}
java -jar Ais2map-0.0.2-SNAPSHOT-jar-with-dependencies.jar --ais-address 192.168.178.20 --logging-dir "" --speech-synthesizer-bin /usr/bin/pico2wave --speech-synthesizer-args "-w <OUTFILE> '<TEXT>'" > ${LOG_FILE} 2>&1 &

echo "Start Node.js...";
cd ${AIS_2_MAP_DIR}/src/main/resources/server/
HTTP_PORT=8383 UDP_ADDRESS=127.0.0.1 UDP_PORT=10111 DEBUG=1 node main.js > ${LOG_FILE} 2>&1 &
