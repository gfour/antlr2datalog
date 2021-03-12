package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

/**
 * The parser configurations to use. This is a sample, extend as needed.
 */
public enum ParserConfiguration {
    // C (C11), BSD license
    C("C", "CLexer", "CParser", "compilationUnit", Arrays.asList(".c", ".h"), "C/1.0-SNAPSHOT/C-1.0-SNAPSHOT.jar", true, false),
    // Cobol 85, MIT license
    COBOL85("Cobol85", "Cobol85Lexer", "Cobol85Parser", "startRule", Arrays.asList(".txt", ".cbl"), "cobol85/1.0-SNAPSHOT/cobol85-1.0-SNAPSHOT.jar", true, false),
    // C++, MIT license
    CPP("C++", "CPP14Lexer", "CPP14Parser", "translationUnit", Arrays.asList(".cpp", ".hpp", ".c", ".h"), "CPP14/1.0-SNAPSHOT/CPP14-1.0-SNAPSHOT.jar", true, false),
    // Go, BSD license
    GO("Go", "GoLexer", "GoParser", "sourceFile", Collections.singletonList(".go"), "golang/1.0-SNAPSHOT/golang-1.0-SNAPSHOT.jar", true, false),
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
    private final String lexerClassName;
    /** The (fully qualified) class name of the parser. */
    private final String parserClassName;
    /** The name of the root node in the grammar. */
    private final String rootNode;
    /** The file extensions recognized by the parser. */
    final Collection<String> extensions;
    /** The parser JAR path in the local Maven repo (suffix). */
    private final String jarPath;
    /**
     * If true, use special grammars-v4 path prefix in local Maven repo.
     * If false, use the extra-grammars directory.
     */
    private final boolean isAntlrGrammars;
    /** If true, use lowercase character stream (for case-insensitive languages such as PHP). */
    public final boolean lowerCase;
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

    /**
     * Loads the lexer/parser JAR and resolves their Class objects.
     * @param debug                    debug mode
     * @throws MalformedURLException   on bad local paths
     * @throws ClassNotFoundException  on bad JAR contents
     * @throws NoSuchMethodException   on bad parser configuration
     */
    public void load(boolean debug) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException {
        String jarPath = getJarPath(debug);
        System.out.println("Using JAR: " + jarPath);
        ClassLoader loader = new URLClassLoader(new URL[] { new URL("file://" + jarPath) }, this.getClass().getClassLoader());
        this.lexerClass = (Class<? extends Lexer>)loader.loadClass(lexerClassName);
        this.parserClass = (Class<? extends Parser>)loader.loadClass(parserClassName);
        this.rootNodeMethod = parserClass.getDeclaredMethod(rootNode);
    }

    public static String[] valuesLowercase() {
        return Arrays.stream(values()).map((ParserConfiguration pc) -> pc.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);
    }

    /**
     * This method resolves the parser JAR. It may look into the bundled
     * resources, the local Maven repository, or some custom path.
     * @param debug                    debug mode
     * @return the path of the parser JAR
     */
    private String getJarPath(boolean debug) {
        try {
            return Resources.extractResourceFile(getClass().getClassLoader(), "parsers/" + jarPath);
        } catch (Exception ignored) {
            if (debug)
                System.out.println("No bundled parser, attempting resolution via file system...");
        }
        String homeDir = System.getProperty("user.home");
        return (isAntlrGrammars ? homeDir + "/.m2/repository/" + ANTLR_GRAMMARS_PREFIX : (new File("extra-grammars").getAbsolutePath() + "/")) + this.jarPath;
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
