package me.tomassetti.bytecode_generation.pushop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PushBoolean extends BytecodeSequence {

    private boolean value;

    public PushBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public void operate(MethodVisitor mv) {
        if (value) {
            mv.visitInsn(Opcodes.ICONST_1);
        } else {
            mv.visitInsn(Opcodes.ICONST_0);
        }
    }
}
