package me.tomassetti.turin.compiler.bytecode.logicalop;

import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
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
