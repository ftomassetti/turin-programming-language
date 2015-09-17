package me.tomassetti.turin.parser.analysis.resolvers.jar;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.NotFoundException;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public enum ClassPoolFactory {

    INSTANCE;

    // true means use the system path
    private ClassPool classPool = new ClassPool(true);

    public void addJar(ClassPath classPath) {
        classPool.appendClassPath(classPath);
    }

    public ClassPool getClassPool() {
        return classPool;
    }
}
