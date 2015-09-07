package me.tomassetti.turin.compiler.bytecode.pushop;

import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
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
