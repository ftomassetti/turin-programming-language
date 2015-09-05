package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ReturnValue extends BytecodeSequence {

    private int returnType;
    private BytecodeSequence pushValue;

    public ReturnValue(int returnType, BytecodeSequence pushValue) {
        this.returnType = returnType;
        this.pushValue = pushValue;
    }

    @Override
    public void operate(MethodVisitor mv) {
        pushValue.operate(mv);
        mv.visitInsn(returnType);
    }

}
