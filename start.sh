#!/bin/bash
./gradlew clean build
java -jar ./build/libs/offer-service-0.0.1-SNAPSHOT.jar --server.port=8080