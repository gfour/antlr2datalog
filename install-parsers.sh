#!/usr/bin/env bash

set -e

echo "* Updating parsers submodule..."
git submodule update --init

echo "* Building parser dependencies..."
cd grammars-v4
mvn install --pl c --pl cobol85 --pl cpp --pl kotlin/kotlin-formal --pl lua --pl php --pl python/python3 --pl rust --am
cd ..
