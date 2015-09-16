package me.tomassetti.turin;

import me.tomassetti.turin.jvm.JvmNameUtils;

/**
 * ClassLoader use to load compiled classes.
 */
public class TurinClassLoader extends ClassLoader {

    public Class<?> addClass(String name, byte[] bytecode){
        if (!JvmNameUtils.isValidQualifiedName(name)) {
            throw new IllegalArgumentException();
        }
        return defineClass(name, bytecode, 0, bytecode.length);
    }

}
