package me.tomassetti.bytecode_generation;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ATHROW;

public class ThrowBS extends BytecodeSequence {

    private BytecodeSequence pushException;

    public ThrowBS(BytecodeSequence pushException) {
        this.pushException = pushException;
    }

    @Override
    public void operate(MethodVisitor mv) {
        pushException.operate(mv);
        mv.visitInsn(ATHROW);
    }

}
