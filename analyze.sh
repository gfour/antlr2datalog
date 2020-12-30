#!/usr/bin/env bash

set -e

function compileAndRunLogic() {
    echo "Compiling logic..."
    souffle -c logic-out.dl -o analyzer
    echo "Running logic..."
    /usr/bin/time ./analyzer -D out-db -F out-facts
}

function interpretLogic() {
    /usr/bin/time souffle logic-out.dl -F out-facts -D out-db
}

function analyze() {
    pushd workspace &> /dev/null
    echo "Running analysis..."
    rm -rf out-db
    mkdir -p out-db
    cpp -P "../${LOGIC}" -o logic-out.dl
    # compileAndRunLogic
    interpretLogic
    popd &> /dev/null
}

function useKotlin() {
   ./gradlew run --args="-l kotlin -i grammars-v4/kotlin/kotlin-formal/examples/Test.kt -f out-facts"
    LOGIC=logic/kotlin-logic.dl
}

function useCobol() {
    ./gradlew run --args="-l cobol -i grammars-v4/cobol85/examples/example1.txt -f workspace/out-facts"
    LOGIC=logic/cobol-logic.dl
}

function usePython3() {
    ./gradlew run --args="-l python3 -i grammars-v4/python/python3/examples/coroutines.py -f workspace/out-facts"
    LOGIC=logic/python3-logic.dl
}


mkdir -p workspace
rm -rf workspace/out-facts

# Only uncomment one of the following.
useKotlin
# useCobol
# usePython3

analyze
