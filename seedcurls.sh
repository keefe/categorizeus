#!/bin/bash
curl localhost:8080/msg/1
curl -XPOST -H 'Content-Type: application/json' -d'{"tags":["tag1"]}' localhost:8080/tag/
