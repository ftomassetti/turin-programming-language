package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IRETURN;

public class ReturnFalse extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
    }

}
