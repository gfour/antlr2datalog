package org.clyze.antlr2datalog;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

/**
 * The parser configurations to use. This is a sample, extend as needed.
 */
public enum ParserConfiguration {
    // C (C11), BSD license
    C("C", "CLexer", "CParser", "compilationUnit", Collections.singletonList(".c"), "C/1.0-SNAPSHOT/C-1.0-SNAPSHOT.jar", true),
    // Cobol 85, MIT license
    COBOL85("Cobol85", "Cobol85Lexer", "Cobol85Parser", "startRule", Arrays.asList(".txt", ".cbl"), "cobol85/1.0-SNAPSHOT/cobol85-1.0-SNAPSHOT.jar", true),
    // C++, MIT license
    CPP("C++", "CPP14Lexer", "CPP14Parser", "translationUnit", Arrays.asList(".cpp", ".c"), "CPP14/1.0-SNAPSHOT/CPP14-1.0-SNAPSHOT.jar", true),
    // Kotlin, Apache 2.0 license
    KOTLIN("Kotlin", "KotlinLexer", "KotlinParser", "kotlinFile", Collections.singletonList(".kt"), "kotlin-formal/1.0-SNAPSHOT/kotlin-formal-1.0-SNAPSHOT.jar", true),
    // Lua, BSD license
    LUA("Lua", "LuaLexer", "LuaParser", "chunk", Collections.singletonList(".lua"), "Lua/1.0-SNAPSHOT/Lua-1.0-SNAPSHOT.jar", true),
    // Python3, MIT license
    PYTHON3("Python3", "Python3Lexer", "Python3Parser", "file_input", Collections.singletonList(".py"), "python3/1.0-SNAPSHOT/python3-1.0-SNAPSHOT.jar", true),
    // Rust, MIT license
    RUST("Rust", "RustLexer", "RustParser", "crate", Collections.singletonList(".rs"), "rust/1.0-SNAPSHOT/rust-1.0-SNAPSHOT.jar", true),
    ;

    /** The path in the local Maven repo for grammars-v4 grammars. */
    final static String ANTLR_GRAMMARS_PREFIX = "org/antlr/grammars/";

    /** The friendly name of the configuration. */
    public final String name;
    /** The (fully qualified) class name of the lexer. */
    private final String lexerClassName;
    /** The (fully qualified) class name of the parser. */
    private final String parserClassName;
    /** The name of the root node in the grammar. */
    private final String rootNode;
    /** The file extensions recognized by the parser. */
    final Collection<String> extensions;
    /** The parser JAR path in the local Maven repo (suffix). */
    private final String jarPath;
    /** If true, use special path prefix in local Maven repo. */
    private final boolean isAntlrGrammars;
    /** The resolved (by load method) lexer Class. */
    public Class<? extends Lexer> lexerClass = null;
    /** The resolved (by load method) parser Class. */
    public Class<? extends Parser> parserClass = null;
    /** The resolved (by load method) root node Method. */
    public Method rootNodeMethod = null;

    /**
     * Create a new parser configuration.
     * @param name               the friendly name of the configuration
     * @param lexerClassName     the (fully qualified) class name of the lexer
     * @param parserClassName    the (fully qualified) class name of the parser
     * @param rootNode           the name of the root node in the grammar
     * @param extensions         file extensions recognized by the parser
     * @param jarPath            the parser JAR path in the local Maven repo (suffix if isAntlrGrammars is true, full path otherwise)
     * @param isAntlrGrammars    if true, use the local Maven repo prefix for grammars-v4
     */
    ParserConfiguration(String name, String lexerClassName, String parserClassName, String rootNode,
                        Collection<String> extensions, String jarPath, boolean isAntlrGrammars) {
        this.name = name;
        this.lexerClassName = lexerClassName;
        this.parserClassName = parserClassName;
        this.rootNode = rootNode;
        this.extensions = extensions;
        this.jarPath = jarPath;
        this.isAntlrGrammars = isAntlrGrammars;
    }

    /**
     * Loads the lexer/parser JAR and resolves their Class objects.
     * @throws MalformedURLException   on bad local paths
     * @throws ClassNotFoundException  on bad JAR contents
     */
    public void load() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException {
        String homeDir = System.getProperty("user.home");
        String jarPath = "file://" + (isAntlrGrammars ? homeDir + "/.m2/repository/" + ANTLR_GRAMMARS_PREFIX : "") + this.jarPath;
        System.out.println("Using JAR: " + jarPath);
        URL[] urls = new URL[] { new URL(jarPath) };
        ClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
        this.lexerClass = (Class<? extends Lexer>)loader.loadClass(lexerClassName);
        this.parserClass = (Class<? extends Parser>)loader.loadClass(parserClassName);
        this.rootNodeMethod = parserClass.getDeclaredMethod(rootNode);
    }

    public static String[] valuesLowercase() {
        return Arrays.stream(values()).map((ParserConfiguration pc) -> pc.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);
    }
}