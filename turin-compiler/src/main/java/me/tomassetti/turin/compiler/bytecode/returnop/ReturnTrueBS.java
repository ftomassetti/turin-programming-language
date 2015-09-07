package me.tomassetti.turin.compiler.bytecode.returnop;

import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IRETURN;

public class ReturnTrueBS extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
    }

}
