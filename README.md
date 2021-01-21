# antlr2datalog

This program transforms ANTLR parsers to
[Datalog](https://github.com/souffle-lang/souffle/) front ends. It
currently uses the parsers from the
[grammars-v4](https://github.com/antlr/grammars-v4) repository.

The ANTLR parser API is translated into a Datalog schema and the
parser can then be invoked on source code to populate a "facts"
directory according to the schema.

Three sample cases are included: COBOL, Kotlin, and Python3.

## Installation

* Install the "grammars-v4" parsers:

```
git clone https://github.com/antlr/grammars-v4.git
cd grammars-v4
mvn install
```

* Install [Souffle](https://github.com/souffle-lang/souffle/).

* Build and install the distribution:

```
./gradlew installDist
```

## Use

Python3 example:

```
build/install/antlr2datalog/bin/antlr2datalog -l python3 -i grammars-v4/python/python3/examples/coroutines.py
```

Kotlin example:

```
build/install/antlr2datalog/bin/antlr2datalog -l kotlin -i grammars-v4/kotlin/kotlin-formal/examples/Test.kt
```

## Adding a language/parser

1. Edit class ParserConfiguration to add a case for your parser.

2. Add `logic/LANGUAGE-logic.dl` with your Datalog rules.

## License

For the license of this project, see [LICENSE](LICENSE). Note that
ANTLR parsers may be covered by different licenses.
