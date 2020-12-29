#!/usr/bin/env bash

set -e

function analyze() {
    echo "Running analysis..."
    rm -rf out-db
    mkdir -p out-db
    cpp -P "${LOGIC}" -o logic-out.dl
    # souffle -c logic-out.dl -o logic
    # ./logic -D out-db -F out-facts
    souffle logic-out.dl -F out-facts -D out-db
}

function useKotlin() {
    ./gradlew run --args="-l kotlin -i grammars-v4/kotlin/kotlin-formal/examples/Test.kt -f out-facts"
    LOGIC=kotlin-logic.dl
}

function useCobol() {
    ./gradlew run --args="-l cobol -i grammars-v4/cobol85/examples/example1.txt -f out-facts"
    LOGIC=cobol-logic.dl
}

function useSolidity() {
    ./gradlew run --args="-l solidity -i grammars-v4/solidity/test.sol -f out-facts"
    LOGIC=solidity-logic.dl
}

rm -rf out-facts

# Only uncomment one of the following.
# useKotlin
# useCobol
useSolidity

analyze
