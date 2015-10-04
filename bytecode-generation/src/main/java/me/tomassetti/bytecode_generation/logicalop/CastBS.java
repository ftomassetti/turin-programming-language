package me.tomassetti.bytecode_generation.logicalop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CastBS extends BytecodeSequence {

    private String type;

    public CastBS(String type) {
        this.type = type;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.CHECKCAST, type);
    }
}
