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
        } else if (jvmType.equals(new JvmType("Z"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Boolean", "booleanValue", "()Z", false, false)).operate(mv);
        } else if (jvmType.equals(new JvmType("C"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Character", "charValue", "()C", false, false)).operate(mv);
        } else if (jvmType.equals(new JvmType("B"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Byte", "byteValue", "()B", false, false)).operate(mv);
        } else if (jvmType.equals(new JvmType("S"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Short", "shortValue", "()S", false, false)).operate(mv);
        } else if (jvmType.equals(new JvmType("J"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Long", "longValue", "()J", false, false)).operate(mv);
        } else if (jvmType.equals(new JvmType("F"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Float", "floatValue", "()F", false, false)).operate(mv);
        } else if (jvmType.equals(new JvmType("D"))) {
            new MethodInvocationBS(new JvmMethodDefinition("java/lang/Double", "doubleValue", "()D", false, false)).operate(mv);
        } else {
            throw new UnsupportedOperationException(jvmType.toString());
        }
    }
}
