package me.tomassetti.bytecode_generation;

import me.tomassetti.jvm.JvmConstructorDefinition;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class NewInvocationBS extends BytecodeSequence {

    private String type;
    private BytecodeSequence argumentsPush;
    private String descriptor;

    public NewInvocationBS(JvmConstructorDefinition constructorDefinition, BytecodeSequence argumentsPush) {
        this.type = constructorDefinition.getOwnerInternalName();
        this.argumentsPush = argumentsPush;
        this.descriptor = constructorDefinition.getDescriptor();
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, type);
        // The first type is consumed by new, the second by the constructor
        mv.visitInsn(DUP);
        argumentsPush.operate(mv);
        // false because the method is not declared on an interface
        mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", descriptor, false);
    }

}
