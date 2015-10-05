package me.tomassetti.bytecode_generation.pushop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;

/**
 * Put a direct value in the stack.
 */
public class PushFloatConst extends BytecodeSequence {

    private float value;

    public PushFloatConst(float value) {
        this.value = value;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitLdcInsn(value);
    }
}
