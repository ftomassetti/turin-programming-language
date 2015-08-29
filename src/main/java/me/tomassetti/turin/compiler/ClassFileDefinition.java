package me.tomassetti.turin.compiler;

/**
 * Created by federico on 29/08/15.
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
        this.name = name;
        this.bytecode = bytecode;

    }
}
