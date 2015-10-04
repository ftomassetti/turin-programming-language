package me.tomassetti.turin;

import me.tomassetti.turin.compiler.ClassFileDefinition;
import me.tomassetti.jvm.JvmNameUtils;

/**
 * ClassLoader used to load classes compiled from Turin.
 */
public class TurinClassLoader extends ClassLoader {

    public Class<?> addClass(ClassFileDefinition classFileDefinition){
        return addClass(classFileDefinition.getName(), classFileDefinition.getBytecode());
    }

    public Class<?> addClass(String name, byte[] bytecode){
        if (!JvmNameUtils.isValidQualifiedName(name)) {
            throw new IllegalArgumentException();
        }
        return defineClass(name, bytecode, 0, bytecode.length);
    }

}
