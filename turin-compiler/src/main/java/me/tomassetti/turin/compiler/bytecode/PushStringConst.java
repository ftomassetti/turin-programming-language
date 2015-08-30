package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by federico on 29/08/15.
 */
public class PushStringConst extends BytecodeSequence {

    private String value;

    public PushStringConst(String value) {
        this.value = value;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitLdcInsn(value);
    }
}
