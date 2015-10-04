package me.tomassetti.bytecode_generation.pushop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;

/**
 * Push a String into the stack.
 */
public class PushStringConst extends BytecodeSequence {

    private String value;

    public PushStringConst(String value) {
        this.value = value;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitLdcInsn(value);
    }
}
