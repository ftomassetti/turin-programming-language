package me.tomassetti.turin.resolvers.compiled;

import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarTypeResolver extends AbstractCompiledTypeResolver<JarClasspathElement> {

    protected File file;

    /**
     * Note that it adds itself in the global ClassPool.
     */
    public JarTypeResolver(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Null is not an acceptable value for file");
        }
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Not existing or not a file: " + file.getPath());
        }
        this.file = file;
        JarFile jarFile = new JarFile(file.getPath());
        JarEntry entry = null;
        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); entry = e.nextElement()) {
            if (entry != null && !entry.isDirectory() && entry.getName().endsWith(".class")) {
                if (file.getName().startsWith(FunctionDefinitionNode.CLASS_PREFIX)) {
                    String name = classFileToFunctionName(entry.getName());
                    if (!JvmNameUtils.isSimpleName(name)) {
                        packages.add(JvmNameUtils.getPackagePart(name));
                    }
                    functionElements.put(name, new JarClasspathElement(this, jarFile, entry, name));
                } else {
                    String name = entryPathToClassName(entry.getName());
                    if (!JvmNameUtils.isSimpleName(name)) {
                        packages.add(JvmNameUtils.getPackagePart(name));
                    }
                    classpathElements.put(name, new JarClasspathElement(this, jarFile, entry, name));
                }
            }
        }
        ClassPoolFactory.INSTANCE.addJar(new CompiledClassPath());
    }

    private String classFileToFunctionName(String classFile){
        String functionName = classFile;
        functionName = functionName.replaceAll("/", ".");
        functionName = functionName.replaceAll("\\$", ".");
        functionName += ".";
        functionName = functionName.substring(0, functionName.length() - ".class".length());

        int index = functionName.lastIndexOf('.');
        // remove the 'Function_' prefix
        functionName = functionName.substring(0, index) + functionName.substring(index + FunctionDefinitionNode.CLASS_PREFIX.length());

        return functionName;
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

}
