package me.tomassetti.turin.compiler.bytecode.returnop;

import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ReturnVoidBS extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(Opcodes.RETURN);
    }

}
