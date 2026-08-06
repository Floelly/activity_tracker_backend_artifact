#!/bin/bash
which mvn 2>&1 || echo "mvn not found"
which java 2>&1 || echo "java not found"
java -version 2>&1 || echo "java not available"
cd /workspace && mvn --version 2>&1 || echo "mvn not working"