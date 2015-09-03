package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by federico on 29/08/15.
 */
public class MethodInvocation extends BytecodeSequence {

    private JvmMethodDefinition jvmMethodDefinition;

    public MethodInvocation(JvmMethodDefinition jvmMethodDefinition) {
        this.jvmMethodDefinition = jvmMethodDefinition;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKEVIRTUAL, jvmMethodDefinition.getJvmType().replaceAll("\\.", "/"), jvmMethodDefinition.getName(), jvmMethodDefinition.getSignature(), jvmMethodDefinition.isOnInterface());
    }

}
