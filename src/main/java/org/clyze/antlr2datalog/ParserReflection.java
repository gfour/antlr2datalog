package org.clyze.antlr2datalog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

/**
 * This class keeps reflection objects (such as Class/Method instances) for
 * instantiations of parser configurations. This separation between configuration
 * and reflective instantiation is needed to isolate reflection metadata in
 * the presence of different classloaders, JVM instances, or Gradle workers.
 */
public class ParserReflection {
    /** The parser configuration used. */
    public final ParserConfiguration pc;
    /** The lexer Class (resolved by load method). */
    public Class<? extends Lexer> lexerClass = null;
    /** The parser Class (resolved by load method). */
    public Class<? extends Parser> parserClass = null;
    /** The root node Method (resolved by load method). */
    public Method rootNodeMethod = null;
    /** The subtypes map (for node types that are implemented as a type hierarchy). */
    Map<String, Collection<Class<?>>> subtypes;

    /**
     * Loads the lexer/parser JAR and resolves their Class objects.
     * @param pc                       the parser configuration to use
     * @param debug                    debug mode
     * @throws MalformedURLException   on bad local paths
     * @throws ClassNotFoundException  on bad JAR contents
     * @throws NoSuchMethodException   on bad parser configuration
     */
    public ParserReflection(ParserConfiguration pc, boolean debug)
            throws MalformedURLException, ClassNotFoundException, NoSuchMethodException {
        this.pc = pc;

        String fullJarPath = getFullJarPath(pc, debug);
        System.out.println("Using JAR: " + fullJarPath);
        ClassLoader loader = new URLClassLoader(new URL[]{new URL("file://" + fullJarPath)}, this.getClass().getClassLoader());

        this.lexerClass = (Class<? extends Lexer>) loader.loadClass(pc.lexerClassName);
        this.parserClass = (Class<? extends Parser>) loader.loadClass(pc.parserClassName);
        this.rootNodeMethod = parserClass.getDeclaredMethod(pc.rootNode);
        this.subtypes = computeSubtypes(loader, fullJarPath, debug);

        System.out.println("Parser configuration (" + pc.name + ") initialized: " + hashCode());
    }

    /**
     * Record A-is-subtype-of-B for classes in the parser. This is used to
     * create accessors for nodes that are represented at runtime using
     * subtyping.
     * @param loader        the parser JAR class loader
     * @param fullJarPath   the file path of the parser JAR
     * @param debug         debug mode
     * @return              a table from supertypes to subtypes
     */
    private static Map<String, Collection<Class<?>>> computeSubtypes(ClassLoader loader, String fullJarPath, boolean debug) {
        Map<String, Collection<Class<?>>> subtypes = new HashMap<>();
        try (JarFile jar = new JarFile(fullJarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - ".class".length()).replaceAll("/", ".");
                    try {
                        Class<?> c = loader.loadClass(className);
                        Class<?> cSuper = c.getSuperclass();
                        if (cSuper != null) {
                            String superclass = cSuper.getCanonicalName();
                            if (!superclass.startsWith("org.antlr.v4.runtime"))
                                subtypes.computeIfAbsent(superclass, s -> new ArrayList<>()).add(c);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (debug) {
            System.out.println("Subtypes map:");
            subtypes.forEach((k, v) -> System.out.println(k + " -> " + v));
        }
        return subtypes;
    }


    /**
     * This method resolves the parser JAR. It may look into the bundled
     * resources, the local Maven repository, or some custom path.
     * @param pc       the parser configuration to use
     * @param debug    debug mode
     * @return         the path of the parser JAR
     */
    private static String getFullJarPath(ParserConfiguration pc, boolean debug) {
        String jarPath = pc.jarPath;
        try {
            return Resources.extractResourceFile(ParserReflection.class.getClassLoader(), "parsers/" + jarPath);
        } catch (Exception ignored) {
            if (debug)
                System.out.println("No bundled parser, attempting resolution via file system...");
        }
        String homeDir = System.getProperty("user.home");
        return (pc.isAntlrGrammars ? homeDir + "/.m2/repository/" + ParserConfiguration.ANTLR_GRAMMARS_PREFIX : (new File("extra-grammars").getAbsolutePath() + "/")) + jarPath;
    }
}
