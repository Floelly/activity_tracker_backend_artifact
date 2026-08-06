#!/bin/bash
cd /workspace && mvn test -Dtest="ActivityServiceTest,ActivityControllerTest" 2>&1