#!/bin/bash

sudo chown -R ubuntu:ubuntu /home/ubuntu
cd /home/ubuntu/mobapp-MQTT-AndroidHub
mv mobapp-MQTT-AndroidHub.apk /home/ubuntu/apps
# sudo docker-compose stop
# sudo docker-compose rm -f
# node dist/main