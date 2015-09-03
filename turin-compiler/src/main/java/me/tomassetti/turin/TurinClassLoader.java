package me.tomassetti.turin;

/**
 * ClassLoader use to load compiled classes.
 */
public class TurinClassLoader extends ClassLoader {

    public Class<?> addClass(String name, byte[] bytecode){
        if (name.contains("/")) {
            throw new IllegalArgumentException();
        }
        return defineClass(name, bytecode, 0, bytecode.length);
    }

}
