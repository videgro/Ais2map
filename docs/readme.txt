
https://github.com/dma-ais/AisLib
DMA AisLib is a Java library for handling AIS messages. 

cd AisLib
mvn clean install (Java 8)
-----

RaspberryPI
sudo apt-get install openjdk-8-jre-headless npm nodejs-legacy espeak

mkdir ~/ais/ais2map
mkdir ~/ais/ais2map/src
mkdir ~/ais/ais2map/src/main
cd ~/ais/ais2map

From development machine
scp ~/workspaces/workspace_github/Ais2map/target/Ais2map-0.0.2-SNAPSHOT-jar-with-dependencies.jar pi@raspberrypi:/home/pi/ais/ais2map/
scp -R ~/workspaces/workspace_github/Ais2map/resources pi@raspberrypi:/home/pi/ais/ais2map/src/main/ 

At RaspberryPi
cd /home/pi/ais/ais2map/src/main/resources/server

Install node.js packages
npm install express morgan socket.io

//Start Node.js
//HTTP_PORT=8383 UDP_PORT=10111 DEBUG=false node main.js

ln -s /home/pi/ais/ais2map/src/main/resources/linux/ais2map-java.service /etc/systemd/system/ais2map-java.service
ln -s /home/pi/ais/ais2map/src/main/resources/linux/ais2map-nodejs.service /etc/systemd/system/ais2map-nodejs.service

ln -s /etc/systemd/system/ais2map-java.service /etc/systemd/system/multi-user.target.wants/ais2map-java.service
ln -s /etc/systemd/system/ais2map-nodejs.service /etc/systemd/system/multi-user.target.wants/ais2map-nodejs.service

sudo systemctl daemon-reload

From webserver
Forward requests to RaspberryPi

Apache
# Enabled Modules: proxy, proxy_http
SSLProxyEngine on
SSLProxyVerify none
SSLProxyCheckPeerCN off
SSLProxyCheckPeerName off
SSLProxyCheckPeerExpire off

ProxyPass        /ships/ http://raspberrypi:8383/

-----------------------------------------
READ MORE
http://catb.org/gpsd/AIVDM.html
http://www.navcen.uscg.gov/?pageName=AISMessagesAStatic
http://www.bosunsmate.org/ais/message5.php

----


sudo rmmod dvb_usb_rtl28xxu ??


TTS
sudo apt-get install libttspico-utils
pico2wave -w lookdave.wav "Look Dave, I can see you're really upset about this." && aplay lookdave.wav
