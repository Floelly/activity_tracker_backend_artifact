#!/bin/bash
cd /workspace
mkdir -p target/surefire-reports
./mvnw test 2>&1