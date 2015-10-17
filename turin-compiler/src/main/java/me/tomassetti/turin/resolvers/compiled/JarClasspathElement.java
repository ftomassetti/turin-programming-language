package me.tomassetti.turin.resolvers.compiled;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class JarClasspathElement implements ClasspathElement {
    private JarTypeResolver jarTypeResolver;
    private JarFile jarFile;
    private JarEntry entry;
    private String path;

    public JarClasspathElement(JarTypeResolver jarTypeResolver, JarFile jarFile, JarEntry entry, String path) {
        this.jarTypeResolver = jarTypeResolver;
        this.jarFile = jarFile;
        this.entry = entry;
        this.path = path;
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return jarFile.getInputStream(entry);
    }

    @Override
    public CtClass toCtClass() throws IOException {
        InputStream is = toInputStream();
        ClassPool classPool = ClassPoolFactory.INSTANCE.getClassPool();
        CtClass ctClass = classPool.makeClass(is);
        return ctClass;
    }

    @Override
    public URL toURL() {
        String urlContent = "jar:file:"+ jarTypeResolver.file.getAbsolutePath()+"!/"+entry.getName();
        try {
            return new URL(urlContent);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL was: " + urlContent, e);
        }
    }
}
