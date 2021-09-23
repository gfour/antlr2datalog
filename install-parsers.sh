#!/usr/bin/env bash

set -e

echo "* Updating parsers submodule..."
git submodule update --init

echo "* Building parser dependencies..."
cd grammars-v4
mvn install --pl c --pl cobol85 --pl cpp --pl golang --pl javascript/javascript --pl kotlin/kotlin-formal --pl lua --pl php --pl prolog --pl python/python3 --pl rust --am
cd ..

echo "* Building extra grammars..."
cd extra-grammars
ANTLR_JAR=antlr-4.9.2-complete.jar
if [ ! -f "${ANTLR_JAR}" ]; then
    curl https://www.antlr.org/download/${ANTLR_JAR} -o ${ANTLR_JAR}
fi
echo "* Building Solidity parser..."
cd solidity-parser
java -jar ../antlr-4.9.2-complete.jar *.g4
javac -cp ../antlr-4.9.2-complete.jar *.java
rm -rf target
mkdir -p target
mv *.class target
jar -cf solidity.jar -C target .
rm *.java *.tokens *.interp
cd ..
mkdir -p solidity
mv solidity-parser/solidity.jar solidity
cd ..
