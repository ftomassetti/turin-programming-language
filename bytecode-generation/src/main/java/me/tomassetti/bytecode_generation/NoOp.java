package me.tomassetti.bytecode_generation;

import org.objectweb.asm.MethodVisitor;

/**
 * Placeholder to indicate that nothing needs to be done.
 */
public class NoOp extends BytecodeSequence {

    private static final NoOp INSTANCE = new NoOp();

    private NoOp() {
        // prevent instantiation outside class
    }

    @Override
    public void operate(MethodVisitor mv) {
        // nothing to do
    }

    public static BytecodeSequence getInstance() {
        return INSTANCE;
    }
}
