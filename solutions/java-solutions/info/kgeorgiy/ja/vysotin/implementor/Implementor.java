package info.kgeorgiy.ja.vysotin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation class for {@link Impler} and {@link JarImpler} interfaces.
 * It can generate and pack a simple implementation of given interface
 *
 * 
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Creates an instance of a class
     */
    public Implementor() {}
    /**
     * Constant for lineSeparator
     */
    private final String NEW_LINE = System.lineSeparator();

    /**
     * Generates an implementation of interface and pack it in .jar file
     *
     * @param args array of {@link String}
     *             <ul>
     *             <li>one argument - {@link String} full name of interface to generate realization for</li>
     *             <li>three arguments - -jar, name of interface, file.jar</li>
     *             </ul>
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 1 && args.length != 3)) {
            System.out.println("One or three arguments expected");
            return;
        }
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Not null arguments expected");
            return;
        }
        JarImpler implementor = new Implementor();
        try {
            if (args.length == 1) {
                implementor.implement(Class.forName(args[0]), Path.of(System.getProperty("user.dir")));
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Interface is not found");
        }
    }

    /**
     *
     * @param token interface token to create implementation for.
     * @param root root directory for generated implementation.
     * @throws ImplerException if:
     * <ul>
     *     <li>Token is not an interface</li>
     *     <li>Interface is private</li>
     *     <li>Error while trying to create directories</li>
     *     <li>IOException while trying to write the implementation</li>
     * </ul>
     */
    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("Not an interface");
        }
        checkInterface(token);
        Path path = getPath(token, root, ".java");
        createDirectory(path);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(stringToUnicode(generateClass(token)));
        } catch (IOException e) {
            throw new ImplerException("Can not write in file " + path, e);
        }
    }

    /**
     * Creates a jar file with  compiled implementation of given interface with given jarFile path
     *
     * @param token interface token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if
     * <ul>
     *     <li>Token is not an java interface</li>
     *     <li>Interface is private</li>
     *     <li>{@link IOException} while trying to create directories</li>
     *     <li>{@link IOException} while trying to write the implementation</li>
     *     <li>{@link IOException} while trying to write jar file</li>
     *     <li>{@link IOException} while trying to clean temp files</li>
     *     <li>Can not compile implementation</li>
     *     <li>Can not compile implementation</li>
     * </ul>
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        checkInterface(token);
        createDirectory(jarFile);
        Path tempDir;

        try {
            tempDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Can not create temporary directory", e);
        }

        try {
            implement(token, tempDir);
            compile(token, tempDir);
            createJar(token, jarFile, tempDir);
        } finally {
            try {
                cleanFiles(tempDir);
            } catch (IOException e) {
                throw new ImplerException("Can not delete temporary directory", e);
            }
        }
    }

    /**
     * Creates jarFile with generated .class file of interface implementation
     *
     * @param token {@link Class} interface
     * @param jarFile {@link Path} for jar file
     * @param tempDir {@link Path}directory with .class file
     * @throws ImplerException if
     * <ul>
     *     <li>{@link IOException} while trying to create .jar</li>
     * </ul>
     */
    private void createJar(final Class<?> token, final Path jarFile, final Path tempDir) throws ImplerException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Vysotin Danil");

        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(getName(token).replace('.', '/').concat("Impl.class")));
            Files.copy(getPath(token, tempDir, ".class"), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Can not create Jar", e);
        }
    }

    /**
     * Creates name of interface. If interface is not nested then does nothing
     *
     * @param token {@link Class} interface
     * @return {@link String} full name of interface
     */
    public String getName(final Class<?> token) {
        String[] parts = token.getName().split("\\.");
        if (parts.length < 2) {
            return token.getSimpleName();
        }

        parts[parts.length - 1] = token.getSimpleName();
        return String.join(".", parts);
    }

    /**
     * Converts string to unicode for java
     *
     * @param input {@link String}
     * @return generated {@link String} result
     */
    private static String stringToUnicode(final String input) {
        StringBuilder output = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 128) {
                output.append(String.format("\\u%04x", (int) c));
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    /**
     * Compiles interface implementation
     *
     * @param token {@link Class} token
     * @param path {@link Path} of implementation
     * @throws ImplerException if exit code of {@link JavaCompiler} is not 0
     */
    private void compile(final Class<?> token, final Path path) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String sourcePath = getPath(token, path, ".java").toString();
        String classPath = path + File.pathSeparator + getClassPath(token);
        final String[] args = {sourcePath, "-cp", classPath, "-encoding", "UTF-8"};
        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplerException("Can not compile file " + sourcePath);
        }
    }

    /**
     * Creates a {@link String} path for generated .class files of interface implementation
     *
     * @param token {@link Class} interface
     * @return {@link String} path
     */
    private static String getClassPath(final Class<?> token) {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Cleans temp directories and files
     *
     * @param path path of trash
     * @throws IOException if {@link Files}.walk() throws IOException
     */
    private void cleanFiles(final Path path) throws IOException {
        Files.walk(path).map(Path::toFile).forEach(File::delete);
    }

    /**
     * Creates directory for path
     *
     * @param path {@link Path} path
     * @throws ImplerException if an {@link IOException} occurred while creating a directory
     */
    public void createDirectory(final Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can not create directory", e);
            }
        }
    }

    /**
     * Generates String implementation of interface
     *
     * @param token {@link Class} interface
     * @return generated {@link String} implementation
     */
    private String generateClass(final Class<?> token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateHeader(token));
        for (Method method : token.getMethods()) {
            stringBuilder.append(generateMethodImpl(method));
            stringBuilder.append(NEW_LINE);
        }
        stringBuilder.append(generateFooter());
        return stringBuilder.toString();
    }

    /**
     * Generates the header of the interface implementation
     *
     * @param token {@link Class} interface
     * @return generated {@link String} implementation
     */
    private String generateHeader(final Class<?> token) {
        return generatePackageInfo(token) +
                NEW_LINE +
                NEW_LINE +
                generateClassInfo(token) +
                NEW_LINE;
    }

    /**
     * Generates the package info of the interface implementation
     *
     * @param token {@link Class} interface
     * @return generated {@link String} implementation
     */
    private String generatePackageInfo(final Class<?> token) {
        return token.getPackage() == null ? "" : "package " + token.getPackage().getName() + ";";
    }

    /**
     * Generates the class info of the interface implementation
     *
     * @param token {@link Class} interface
     * @return generated {@link String} implementation
     */
    private String generateClassInfo(final Class<?> token) {
        return "public class " + className(token) + " implements " + token.getCanonicalName() + " {";
    }

    /**
     * Generates a method of the interface implementation
     * Returns default value of output parameter
     *
     * @param method {@link Method} method
     * @return generated {@link String} implementation
     */
    private String generateMethodImpl(final Method method) {
        if (Modifier.isPrivate(method.getModifiers()) ||
                method.isDefault() ||
                Modifier.isStatic(method.getModifiers())) {
            return "";
        }
        return "    public " + method.getReturnType().getCanonicalName() + " " + method.getName() +
                "(" + generateMethodParameters(method) + ")" + "{" +
                NEW_LINE +
                "        return " + defaultValue(method.getReturnType()) + ";" +
                NEW_LINE +
                "    }";
    }

    /**
     * Returns default value of given type
     *
     * @param returnType {@link Class} type token
     * @return
     * <ul>
     *     <li>"" for {@link Void}</li>
     *     <li>false for {@link Boolean}</li>
     *     <li>0 for other {@link javax.lang.model.type.PrimitiveType}</li>
     *     <li>null for non-primitive types</li>
     * </ul>
     */
    private String defaultValue(final Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType.equals(Void.TYPE)) {
                return "";
            }
            return returnType.equals(Boolean.TYPE) ? "false" : "0";
        }
        return "null";
    }

    /**
     * Generates the parameters for a method of the interface implementation
     * @param method {@link Method} method
     * @return generated {@link String} parameters
     */
    private String generateMethodParameters(final Method method) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Parameter parameter : method.getParameters()) {
            stringBuilder.append(parameter.getType().getCanonicalName()).append(" ").append(parameter.getName()).append(", ");
        }
        if (!stringBuilder.isEmpty()) {
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }
        return stringBuilder.toString();
    }

    /**
     * Generates the footer of the interface implementation
     *
     * @return "}"
     */
    private String generateFooter() {
        return "}";
    }

    /**
     * Generates the footer for the interface implementation
     *
     * @param token {@link Class}
     * @return name of token + "Impl"
     */
    private String className(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Generates the path for implementation
     *
     * @param token {@link Class} interface
     * @param path {@link Path} directory
     * @param ending {@link String} file ending
     * @return {@link Path} path for implementation
     */
    private Path getPath(final Class<?> token, final Path path, final String ending) {
        return path.resolve(token.getPackage().getName().replace('.', File.separatorChar))
                .resolve(className(token) + ending);
    }

    /**
     * Checks if interface is private
     *
     * @param token {@link Class} interface
     * @throws ImplerException if interface is private
     */
    private void checkInterface(final Class<?> token) throws ImplerException {
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Interface is private");
        }
    }
}
