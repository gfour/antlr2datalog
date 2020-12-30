#!/usr/bin/env bash

set -e

function compileAndRunLogic() {
    echo "Compiling logic..."
    cpp -P "${LOGIC}" -o logic-out.dl
    souffle -c logic-out.dl -o logic
    echo "Running logic..."
    /usr/bin/time ./logic -D out-db -F out-facts
}

function interpretLogic() {
    /usr/bin/time souffle logic-out.dl -F out-facts -D out-db
}

function analyze() {
    echo "Running analysis..."
    rm -rf out-db
    mkdir -p out-db
    # compileAndRunLogic
    interpretLogic
}

function useKotlin() {
    ./gradlew run --args="-l kotlin -i grammars-v4/kotlin/kotlin-formal/examples/Test.kt -f out-facts"
    LOGIC=kotlin-logic.dl
}

function useCobol() {
    ./gradlew run --args="-l cobol -i grammars-v4/cobol85/examples/example1.txt -f out-facts"
    LOGIC=cobol-logic.dl
}

function usePython3() {
    ./gradlew run --args="-l python3 -i grammars-v4/python/python3/examples/coroutines.py -f out-facts"
    LOGIC=python3-logic.dl
}


rm -rf out-facts

# Only uncomment one of the following.
# useKotlin
# useCobol
usePython3

analyze
