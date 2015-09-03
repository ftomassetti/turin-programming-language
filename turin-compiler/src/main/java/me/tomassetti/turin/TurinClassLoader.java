package me.tomassetti.turin;

/**
 * Created by federico on 29/08/15.
 */
public class TurinClassLoader extends ClassLoader {

    public Class<?> addClass(String name, byte[] bytecode){
        if (name.contains("/")) {
            throw new IllegalArgumentException();
        }
        return defineClass(name, bytecode, 0, bytecode.length);
    }

}
