#!/bin/bash
cd /workspace && mvn compile -q 2>&1 | tail -20