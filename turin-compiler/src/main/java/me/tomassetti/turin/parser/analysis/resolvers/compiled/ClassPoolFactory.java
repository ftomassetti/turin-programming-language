package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import javassist.ClassPath;
import javassist.ClassPool;

import java.util.LinkedList;
import java.util.List;

public enum ClassPoolFactory {

    INSTANCE;

    // true means use the system path
    private ClassPool classPool = new ClassPool(true);

    public void addJar(ClassPath classPath) {
        classPool.appendClassPath(classPath);
    }
    public void addClassesDir(ClassPath classPath) {
        classPool.appendClassPath(classPath);
    }

    public ClassPool getClassPool() {
        return classPool;
    }
}
