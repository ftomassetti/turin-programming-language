package me.tomassetti.bytecode_generation;

import org.objectweb.asm.MethodVisitor;

public class MathOperationBS extends BytecodeSequence {

    private int opcode;

    public MathOperationBS(int opcode) {
        this.opcode = opcode;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(this.opcode);
    }
}
