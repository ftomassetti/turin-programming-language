package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class MethodInvocationBS extends BytecodeSequence {

    private JvmMethodDefinition jvmMethodDefinition;

    public MethodInvocationBS(JvmMethodDefinition jvmMethodDefinition) {
        this.jvmMethodDefinition = jvmMethodDefinition;
    }

    @Override
    public void operate(MethodVisitor mv) {
        // The difference between the invokespecial and the invokevirtual instructions is that invokevirtual invokes
        // a method based on the class of the object. The invokespecial instruction is used to invoke instance
        // initialization methods as well as private methods and methods of a superclass of the current class.
        // ref.: http://zeroturnaround.com/rebellabs/java-bytecode-fundamentals-using-objects-and-calling-methods/
        if (jvmMethodDefinition.isStatic()) {
            mv.visitMethodInsn(INVOKESTATIC, jvmMethodDefinition.getOwnerInternalName(),
                    jvmMethodDefinition.getName(), jvmMethodDefinition.getDescriptor(), false);
        } else {
            if (jvmMethodDefinition.isDeclaredOnInterface()) {
                mv.visitMethodInsn(INVOKEINTERFACE, jvmMethodDefinition.getOwnerInternalName(),
                        jvmMethodDefinition.getName(), jvmMethodDefinition.getDescriptor(), true);
            } else {
                mv.visitMethodInsn(INVOKEVIRTUAL, jvmMethodDefinition.getOwnerInternalName(),
                        jvmMethodDefinition.getName(), jvmMethodDefinition.getDescriptor(), false);
            }
        }
    }

}
