#!/bin/bash

REPO='dragonitenetwork/dragonite-java'

docker login -u $DOCKER_USER -p $DOCKER_PASS
docker build . -f Dockerfile -t $REPO
docker build . -f Dockerfile.alpine -t $REPO:alpine
docker tag $REPO $REPO:$TRAVIS_TAG
docker tag $REPO:alpine $REPO:$TRAVIS_TAG-alpine
docker push $REPO
