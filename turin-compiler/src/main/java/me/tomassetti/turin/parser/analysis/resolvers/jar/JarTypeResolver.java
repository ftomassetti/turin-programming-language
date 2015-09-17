package me.tomassetti.turin.parser.analysis.resolvers.jar;

import javassist.ClassPool;
import javassist.CtClass;
import me.tomassetti.turin.parser.analysis.resolvers.TypeResolver;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        CtClass toCtClass() throws IOException {
            InputStream is = jarFile.getInputStream(entry);
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(is);
            return ctClass;
        }
    }

    private Map<String, ClasspathElement> classpathElements = new HashMap<>();

    public JarTypeResolver(File file) throws IOException {
        JarFile jarFile = new JarFile(file.getPath());
        JarEntry entry = null;
        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); entry = e.nextElement()) {
            if (entry != null && !entry.isDirectory() && entry.getName().endsWith(".class")) {
                String name = entryPathToClassName(entry.getName());
                classpathElements.put(name, new ClasspathElement(jarFile, entry, name));
            }
        }
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
