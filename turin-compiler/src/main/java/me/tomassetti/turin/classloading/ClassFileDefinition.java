package me.tomassetti.turin.classloading;

/**
 * Elements necessary to define a class file.
 */
public class ClassFileDefinition {

    private String name;
    private byte[] bytecode;

    public String getName() {
        return name;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public ClassFileDefinition(String name, byte[] bytecode) {
        if (name.contains("/")) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.bytecode = bytecode;
    }
}
