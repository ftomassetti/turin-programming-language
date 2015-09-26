package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import org.objectweb.asm.MethodVisitor;

/**
 * It expects the value to be already on the stack.
 */
public class UnboxBS extends BytecodeSequence {

    private JvmType jvmType;

    public UnboxBS(JvmType jvmType) {
        this.jvmType = jvmType;
    }

    @Override
    public void operate(MethodVisitor mv) {
        if (jvmType.equals(new JvmType("I"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Integer", "intValue", "()I", false, false)).operate(mv);
        } else {
            throw new UnsupportedOperationException(jvmType.toString());
        }
    }
}
