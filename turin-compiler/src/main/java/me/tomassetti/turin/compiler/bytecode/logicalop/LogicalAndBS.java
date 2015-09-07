package me.tomassetti.turin.compiler.bytecode.logicalop;

import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LogicalAndBS extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(Opcodes.IAND);
    }

}
