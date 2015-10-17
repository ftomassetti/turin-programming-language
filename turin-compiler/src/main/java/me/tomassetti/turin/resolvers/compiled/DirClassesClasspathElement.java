package me.tomassetti.turin.resolvers.compiled;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

class DirClassesClasspathElement implements ClasspathElement {
    private File file;
    private String path;

    public DirClassesClasspathElement(File file, String path) {
        this.file = file;
        this.path = path;
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return new FileInputStream(file);
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
        String urlContent = "file:"+file.getAbsolutePath();
        try {
            return new URL(urlContent);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL was: " + urlContent, e);
        }
    }
}
