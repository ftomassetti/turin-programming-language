package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by federico on 29/08/15.
 */
public class PushIntConst extends BytecodeSequence {

    private int value;

    public PushIntConst(int value) {
        this.value = value;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitIntInsn(Opcodes.BIPUSH, value);
    }
}
