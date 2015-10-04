package me.tomassetti.bytecode_generation.pushop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;

/**
 * Push a local variable in the stack.
 */
public class PushLocalVar extends BytecodeSequence {

    private int loadType;
    private int index;

    public PushLocalVar(int loadType, int index) {
        this.loadType = loadType;
        this.index = index;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitVarInsn(loadType, index);
    }
}
