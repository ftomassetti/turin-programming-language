package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.parser.analysis.JvmConstructorDefinition;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ASTORE;

public class NewInvocation extends BytecodeSequence {

    private String type;
    private List<BytecodeSequence> argumentsPush;
    private String signature;

    public NewInvocation(JvmConstructorDefinition constructorDefinition, List<BytecodeSequence> argumentsPush) {
        this.type = constructorDefinition.getJvmType();
        this.argumentsPush = argumentsPush;
        this.signature = constructorDefinition.getSignature();
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, type);
        mv.visitInsn(DUP);
        argumentsPush.forEach((ap)->ap.operate(mv));
        mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", signature, false);
    }

}
