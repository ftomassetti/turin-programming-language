package me.tomassetti.bytecode_generation;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RelationalOperationBS extends BytecodeSequence {

    private int jumpOpcode;

    public RelationalOperationBS(int jumpOpcode) {
        this.jumpOpcode = jumpOpcode;
    }

    @Override
    public void operate(MethodVisitor mv) {
        Label l0 = new Label();
        mv.visitJumpInsn(jumpOpcode, l0);
        mv.visitInsn(Opcodes.ICONST_1);
        Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, l1);
        mv.visitLabel(l0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitLabel(l1);
    }
}
