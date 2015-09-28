package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import me.tomassetti.turin.parser.analysis.resolvers.TypeResolver;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolve types by looking in a dir of class files.
 */
public class DirClassesTypeResolver implements TypeResolver {

    private class ClasspathElement {
        private File file;
        private String path;

        public ClasspathElement(File file, String path) {
            this.file = file;
            this.path = path;
        }

        InputStream toInputStream() throws IOException {
            return new FileInputStream(file);
        }

        CtClass toCtClass() throws IOException {
            InputStream is = toInputStream();
            ClassPool classPool = ClassPoolFactory.INSTANCE.getClassPool();
            CtClass ctClass = classPool.makeClass(is);
            return ctClass;
        }

        public URL toURL() {
            String urlContent = "file:"+file.getAbsolutePath();
            try {
                return new URL(urlContent);
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL was: " + urlContent, e);
            }
        }
    }

    private Map<String, ClasspathElement> classpathElements = new HashMap<>();

    private class DirClassesClassPath implements ClassPath {

        @Override
        public InputStream openClassfile(String qualifiedName) throws NotFoundException {
            try {
                if (classpathElements.containsKey(qualifiedName)) {
                    return classpathElements.get(qualifiedName).toInputStream();
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw new NotFoundException(e.getMessage());
            }
        }

        @Override
        public URL find(String qualifiedName) {
            if (classpathElements.containsKey(qualifiedName)) {
                return classpathElements.get(qualifiedName).toURL();
            } else {
                return null;
            }
        }

        @Override
        public void close() {
            // nothing to do here
        }
    }

    private File dir;

    /**
     * Note that it adds itself in the global ClassPool.
     */
    public DirClassesTypeResolver(File dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Not existing or not a directory: " + dir.getPath());
        }
        this.dir = dir;
        explore(dir);
        ClassPoolFactory.INSTANCE.addJar(new DirClassesClassPath());
    }

    private void explore(File file) {
        if (file.isDirectory()) {

        } else if (file.isFile()) {
            if (file.getName().endsWith(".class")) {
                String name = classFileToClassName(file, dir);
                classpathElements.put(name, new ClasspathElement(file, name));
            }
        }
    }

    private String classFileToClassName(File classFile, File root){
        String absPathFile = classFile.getAbsolutePath();
        String absPathRoot = root.getAbsolutePath();
        if (!(absPathFile.length() > absPathRoot.length())){
            throw new IllegalStateException();
        }
        String relativePath = absPathFile.substring(absPathRoot.length());
        if (!relativePath.endsWith(".class")){
            throw new IllegalStateException();
        }
        String className = relativePath.substring(0, relativePath.length() - ".class".length());
        className = className.replaceAll("/", ".");
        className = className.replaceAll("\\$", ".");
        return className;
    }

    @Override
    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName) {
        if (classpathElements.containsKey(typeName)) {
            try {
                CtClass ctClass = classpathElements.get(typeName).toCtClass();
                return Optional.of(new JavassistTypeDefinition(ctClass));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }

}
