package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

/**
 * Placeholder to indicate that nothing needs to be done.
 */
public class NoOp extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        // nothing to do
    }

}
