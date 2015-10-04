package me.tomassetti.bytecode_generation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ArrayLengthBS extends  BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(Opcodes.ARRAYLENGTH);
    }

}
