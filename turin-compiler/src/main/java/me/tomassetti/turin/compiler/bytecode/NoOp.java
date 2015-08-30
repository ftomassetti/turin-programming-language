package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

/**
 * Created by federico on 29/08/15.
 */
public class NoOp extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        // nothing to do
    }

}
