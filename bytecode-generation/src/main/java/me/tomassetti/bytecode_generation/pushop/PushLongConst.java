package me.tomassetti.bytecode_generation.pushop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Put a direct value in the stack.
 */
public class PushLongConst extends BytecodeSequence {

    private long value;

    public PushLongConst(long value) {
        this.value = value;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitLdcInsn(value);
    }
}
