package me.tomassetti.bytecode_generation.pushop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;

/**
 * Put a direct value in the stack.
 */
public class PushDoubleConst extends BytecodeSequence {

    private double value;

    public PushDoubleConst(double value) {
        this.value = value;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitLdcInsn(value);
    }
}
