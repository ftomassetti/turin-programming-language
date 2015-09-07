package me.tomassetti.turin.compiler.bytecode.pushop;

import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Push a String into the stack.
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
