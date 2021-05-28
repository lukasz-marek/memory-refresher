#!/bin/bash

for file in $1/*
do
  java -jar build/libs/memory-refresher-1.0-SNAPSHOT-all.jar add "$file"
done
