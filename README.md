# antlr2datalog

This program transforms ANTLR parsers to
[Datalog](https://github.com/souffle-lang/souffle/) front ends. It
currently uses the parsers from the
[grammars-v4](https://github.com/antlr/grammars-v4) repository.

The ANTLR parser API is translated into a Datalog schema and the
parser can then be invoked on source code to populate a "facts"
directory according to the schema.

Three sample cases are included: COBOL, Kotlin, and Solidity.

## Use

* Install the "grammars-v4" parsers:

```
git clone https://github.com/antlr/grammars-v4.git
cd grammars-v4
mvn install
```

* Install [Souffle](https://github.com/souffle-lang/souffle/).

* Run analyze.sh to see an example run.

## Adding a language/parser

1. Edit class ParserConfiguration to add a case for your parser.

2. Add XYZ-logic.dl with your Datalog rules.

3. Edit analyze.sh to add a case for your source file.
