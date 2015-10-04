package me.tomassetti.bytecode_generation.returnop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;

public class ReturnValueBS extends BytecodeSequence {

    private int returnType;
    private BytecodeSequence pushValue;

    public ReturnValueBS(int returnType, BytecodeSequence pushValue) {
        this.returnType = returnType;
        this.pushValue = pushValue;
    }

    @Override
    public void operate(MethodVisitor mv) {
        pushValue.operate(mv);
        mv.visitInsn(returnType);
    }

}
