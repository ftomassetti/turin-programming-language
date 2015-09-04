package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.compiler.Compilation;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ALOAD;

/**
 * Push the "this" value in the stack.
 */
public class PushThis extends BytecodeSequence {

    private static PushThis INSTANCE = new PushThis();

    private PushThis() {

    }

    public static PushThis getInstance() {
        return INSTANCE;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
    }
}
