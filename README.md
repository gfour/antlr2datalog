[![Build Status](https://github.com/gfour/antlr2datalog/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=main)](https://github.com/gfour/antlr2datalog/actions)

# antlr2datalog

This is a framework for statically analyzing source code using Datalog
queries.

For the set of supported source languages, see the list of parsers in
the [grammars-v4](https://github.com/antlr/grammars-v4)
repository. The following languages are included out of the box: C,
C++, COBOL85, Go, Kotlin, Lua, PHP, Python3, and Rust. Support for more
languages can be easily added, read on below for how to enable support
for other languages.

This framework works by transforming ANTLR parsers to
[Datalog](https://github.com/souffle-lang/souffle/) front-ends.  An
ANTLR parser API is translated into a Datalog schema and the parser is
then invoked on the source code to populate a "facts" directory
according to the schema. Finally, the analysis logic runs on the facts
and computes the analysis results. For more information, see
[ARCHITECTURE.md](ARCHITECTURE.md).

## Installation

* Install the "grammars-v4" parsers used:

```
./gradlew installParsers
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

1. Put your ANTLR parser in the local Maven repository. For example,
   to pick from the "grammars-v4" parsers, install them as follows:

```
git clone https://github.com/antlr/grammars-v4.git
cd grammars-v4
mvn install
```

2. Edit class ParserConfiguration to add a case for the parser.

3. Add `logic/LANGUAGE-logic.dl` with the Datalog rules for the new language.

## License

For the license of this project, see [LICENSE](LICENSE). Note that
ANTLR parsers may be covered by different licenses.
