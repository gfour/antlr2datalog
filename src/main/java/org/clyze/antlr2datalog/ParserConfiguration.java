package org.clyze.antlr2datalog;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

/**
 * The parser configurations to use. This is a sample, extend as needed.
 */
public enum ParserConfiguration {
    // Cobol 85, MIT license
    COBOL("Cobol85Lexer", "Cobol85Parser", "startRule", Collections.singletonList(".txt"), "cobol85/1.0-SNAPSHOT/cobol85-1.0-SNAPSHOT.jar"),
    // Kotlin, Apache 2.0 license
//    KOTLIN("org.antlr.grammars.KotlinLexer", "org.antlr.grammars.KotlinParser", "kotlinFile", Collections.singletonList(".kt"), "kotlin-formal/1.0-SNAPSHOT/kotlin-formal-1.0-SNAPSHOT.jar"),
    KOTLIN("KotlinLexer", "KotlinParser", "kotlinFile", Collections.singletonList(".kt"), "kotlin-formal/1.0-SNAPSHOT/kotlin-formal-1.0-SNAPSHOT.jar"),
    // Python3, MIT license
    PYTHON3("Python3Lexer", "Python3Parser", "file_input", Collections.singletonList(".py"), "python3/1.0-SNAPSHOT/python3-1.0-SNAPSHOT.jar"),
    ;

    /** The (fully qualified) class name of the lexer. */
    private final String lexerClassName;
    /** The (fully qualified) class name of the parser. */
    private final String parserClassName;
    /** The name of the root node in the grammar. */
    private final String rootNode;
    /** The file extensions recognized by the parser. */
    final Collection<String> extensions;
    /** The parser JAR path in the local Maven repo (suffix). */
    private final String mavenPath;
    /** The resolved (by load method) lexer Class. */
    public Class<? extends Lexer> lexerClass = null;
    /** The resolved (by load method) parser Class. */
    public Class<? extends Parser> parserClass = null;
    /** The resolved (by load method) root node Method. */
    public Method rootNodeMethod = null;

    /**
     * Create a new parser configuration.
     * @param lexerClassName     the (fully qualified) class name of the lexer
     * @param parserClassName    the (fully qualified) class name of the parser
     * @param rootNode           the name of the root node in the grammar
     * @param extensions         file extensions recognized by the parser
     * @param mavenPath          the parser JAR path in the local Maven repo (suffix)
     */
    ParserConfiguration(String lexerClassName, String parserClassName, String rootNode, Collection<String> extensions, String mavenPath) {
        this.lexerClassName = lexerClassName;
        this.parserClassName = parserClassName;
        this.rootNode = rootNode;
        this.extensions = extensions;
        this.mavenPath = mavenPath;
    }

    /**
     * Loads the lexer/parser JAR and resolves their Class objects.
     * @throws MalformedURLException   on bad local paths
     * @throws ClassNotFoundException  on bad JAR contents
     */
    public void load() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException {
        String homeDir = System.getProperty("user.home");
        String jarPath = "file://" + homeDir + "/.m2/repository/org/antlr/grammars/" + mavenPath;
        System.out.println("Using JAR: " + jarPath);
        URL[] urls = new URL[] { new URL(jarPath) };
        ClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
        this.lexerClass = (Class<? extends Lexer>)loader.loadClass(lexerClassName);
        this.parserClass = (Class<? extends Parser>)loader.loadClass(parserClassName);
        this.rootNodeMethod = parserClass.getDeclaredMethod(rootNode);
    }
}