package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ATHROW;

public class Throw extends BytecodeSequence {

    private BytecodeSequence pushException;

    public Throw(BytecodeSequence pushException) {
        this.pushException = pushException;
    }

    @Override
    public void operate(MethodVisitor mv) {
        pushException.operate(mv);
        mv.visitInsn(ATHROW);
    }

}
