#!/bin/sh

docker-compose -f docker/docker-compose.yml -p v3.3 "$@"
