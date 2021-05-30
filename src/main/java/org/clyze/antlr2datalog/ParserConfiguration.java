package org.clyze.antlr2datalog;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

/**
 * The parser configurations to use. This is a sample, extend as needed.
 */
public enum ParserConfiguration {
    // C (C11), BSD license
    C("C", "CLexer", "CParser", "compilationUnit", Arrays.asList(".c", ".h"), "C/1.0-SNAPSHOT/C-1.0-SNAPSHOT.jar", true, false),
    // Cobol 85, MIT license
    COBOL85("Cobol85", "Cobol85Lexer", "Cobol85Parser", "startRule", Arrays.asList(".txt", ".cbl"), "cobol85/1.0-SNAPSHOT/cobol85-1.0-SNAPSHOT.jar", true, false),
    // C++, MIT license
    CPP("C++", "CPP14Lexer", "CPP14Parser", "translationUnit", Arrays.asList(".cc", ".cpp", ".hpp", ".c", ".h"), "CPP14/1.0-SNAPSHOT/CPP14-1.0-SNAPSHOT.jar", true, false),
    // Go, BSD license
    GO("Go", "GoLexer", "GoParser", "sourceFile", Collections.singletonList(".go"), "golang/1.0-SNAPSHOT/golang-1.0-SNAPSHOT.jar", true, false),
    // JavaScript, MIT license
    JAVASCRIPT("JavaScript", "JavaScriptLexer", "JavaScriptParser", "program", Arrays.asList(".js", ".mjs"), "javascript/1.0-SNAPSHOT/javascript-1.0-SNAPSHOT.jar", true, false),
    // Kotlin, Apache 2.0 license
    KOTLIN("Kotlin", "org.antlr.grammars.KotlinLexer", "org.antlr.grammars.KotlinParser", "kotlinFile", Collections.singletonList(".kt"), "kotlin-formal/1.0-SNAPSHOT/kotlin-formal-1.0-SNAPSHOT.jar", true, false),
    // Lua, BSD license
    LUA("Lua", "LuaLexer", "LuaParser", "chunk", Collections.singletonList(".lua"), "Lua/1.0-SNAPSHOT/Lua-1.0-SNAPSHOT.jar", true, false),
    // PHP, MIT license
    PHP("PHP", "PhpLexer", "PhpParser", "htmlDocument", Collections.singletonList(".php"), "php/1.0-SNAPSHOT/php-1.0-SNAPSHOT.jar", true, true),
    // Python3, MIT license
    PYTHON3("Python3", "Python3Lexer", "Python3Parser", "file_input", Collections.singletonList(".py"), "python3/1.0-SNAPSHOT/python3-1.0-SNAPSHOT.jar", true, false),
    // Rust, MIT license
    RUST("Rust", "RustLexer", "RustParser", "crate", Collections.singletonList(".rs"), "rust/1.0-SNAPSHOT/rust-1.0-SNAPSHOT.jar", true, false),
    // Solidity, MIT license
    SOLIDITY("Solidity", "SolidityLexer", "SolidityParser", "sourceUnit", Collections.singletonList(".sol"), "solidity/solidity.jar", false, false),
    ;

    /** The path in the local Maven repo for grammars-v4 grammars. */
    final static String ANTLR_GRAMMARS_PREFIX = "org/antlr/grammars/";

    /** The friendly name of the configuration. */
    public final String name;
    /** The (fully qualified) class name of the lexer. */
    public final String lexerClassName;
    /** The (fully qualified) class name of the parser. */
    public final String parserClassName;
    /** The name of the root node in the grammar. */
    public final String rootNode;
    /** The file extensions recognized by the parser. */
    final Collection<String> extensions;
    /** The parser JAR path in the local Maven repo (suffix). */
    public final String jarPath;
    /**
     * If true, use special grammars-v4 path prefix in local Maven repo.
     * If false, use the extra-grammars directory.
     */
    public final boolean isAntlrGrammars;
    /** If true, use lowercase character stream (for case-insensitive languages such as PHP). */
    public final boolean lowerCase;

    /**
     * Create a new parser configuration.
     * @param name               the friendly name of the configuration
     * @param lexerClassName     the (fully qualified) class name of the lexer
     * @param parserClassName    the (fully qualified) class name of the parser
     * @param rootNode           the name of the root node in the grammar
     * @param extensions         file extensions recognized by the parser
     * @param jarPath            the parser JAR path in the local Maven repo (suffix if isAntlrGrammars is true, full path otherwise)
     * @param isAntlrGrammars    if true, use the local Maven repo prefix for grammars-v4
     * @param lowerCase          if true, a lower-case character stream is used
     */
    ParserConfiguration(String name, String lexerClassName, String parserClassName, String rootNode,
                        Collection<String> extensions, String jarPath, boolean isAntlrGrammars, boolean lowerCase) {
        this.name = name;
        this.lexerClassName = lexerClassName;
        this.parserClassName = parserClassName;
        this.rootNode = rootNode;
        this.extensions = extensions;
        this.jarPath = jarPath;
        this.isAntlrGrammars = isAntlrGrammars;
        this.lowerCase = lowerCase;
    }

    public static String[] valuesLowercase() {
        return Arrays.stream(values()).map((ParserConfiguration pc) -> pc.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);
    }

    public CharStream getCharStream(String path, InputStream inputStream) throws IOException, UnsupportedParserException {
        if (this.lowerCase) {
            // The required class is not compatible with Java 8. We use reflection below
            // to break compile dependency.
            try {
                Class<? extends CharStream> c = (Class<? extends CharStream>) Class.forName("com.khubla.antlr.antlr4test.filestream.AntlrCaseInsensitiveFileStream");
                Class<?> ciType = Class.forName("com.khubla.antlr.antlr4test.CaseInsensitiveType");
                Constructor<? extends CharStream> constr = c.getConstructor(String.class, String.class, ciType);
                return constr.newInstance(path, "UTF-8", ciType.cast(ciType.getDeclaredField("lower").get(null)));
            } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                System.out.println("ERROR: " + e.getMessage());
                System.out.println("This parser seems to require lowercase char streams. Uncomment dependency 'antlr4test-maven-plugin'");
                System.out.println("and rebuild. Enabling this feature may not support Java 8.");
                throw new UnsupportedParserException();
            }
        } else
            return CharStreams.fromStream(inputStream);
    }

}
