package me.tomassetti.turin.compiler.bytecode.returnop;

import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IRETURN;

public class ReturnFalseBS extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
    }

}
