package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NotOperationBytecode extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        // it is very weird that there is not a single instruction for this...
        Label l0 = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, l0);
        mv.visitInsn(Opcodes.ICONST_1);
        Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, l1);
        mv.visitLabel(l0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitLabel(l1);
    }

}
