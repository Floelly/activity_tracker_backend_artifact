#!/bin/bash
cd /workspace && mvn test-compile -DskipTests 2>&1 | tail -30