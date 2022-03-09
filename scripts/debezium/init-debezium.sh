#!/bin/sh

#sleep 15;

curl -i -X PUT -H "Accept:application/json" -H  "Content-Type:application/json" http://connect:8083/connectors/inventory-connector/config -d @/usr/local/bin/debezium/register-mysql.json