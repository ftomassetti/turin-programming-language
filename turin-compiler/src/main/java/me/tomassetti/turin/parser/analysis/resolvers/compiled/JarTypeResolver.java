package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import me.tomassetti.turin.parser.analysis.resolvers.TypeResolver;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarTypeResolver implements TypeResolver {

    private class ClasspathElement {
        private JarFile jarFile;
        private JarEntry entry;
        private String path;

        public ClasspathElement(JarFile jarFile, JarEntry entry, String path) {
            this.jarFile = jarFile;
            this.entry = entry;
            this.path = path;
        }
        
        InputStream toInputStream() throws IOException {
            return jarFile.getInputStream(entry);
        }

        CtClass toCtClass() throws IOException {
            InputStream is = toInputStream();
            ClassPool classPool = ClassPoolFactory.INSTANCE.getClassPool();
            CtClass ctClass = classPool.makeClass(is);
            return ctClass;
        }

        public URL toURL() {
            String urlContent = "jar:file:"+file.getAbsolutePath()+"!/"+entry.getName();
            try {
                return new URL(urlContent);
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL was: " + urlContent, e);
            }
        }
    }

    private Map<String, ClasspathElement> classpathElements = new HashMap<>();

    private class JarClassPath implements ClassPath {

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

    private File file;

    /**
     * Note that it adds itself in the global ClassPool.
     */
    public JarTypeResolver(File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Not existing or not a file: " + file.getPath());
        }
        this.file = file;
        JarFile jarFile = new JarFile(file.getPath());
        JarEntry entry = null;
        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); entry = e.nextElement()) {
            if (entry != null && !entry.isDirectory() && entry.getName().endsWith(".class")) {
                String name = entryPathToClassName(entry.getName());
                classpathElements.put(name, new ClasspathElement(jarFile, entry, name));
            }
        }
        ClassPoolFactory.INSTANCE.addJar(new JarClassPath());
    }

    private String entryPathToClassName(String entryPath){
        if (!entryPath.endsWith(".class")) {
            throw new IllegalStateException();
        }
        String className = entryPath.substring(0, entryPath.length() - ".class".length());
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
