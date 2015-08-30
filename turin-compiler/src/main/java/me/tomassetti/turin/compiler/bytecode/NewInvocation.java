package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ASTORE;

/**
 * Created by federico on 29/08/15.
 */
public class NewInvocation extends BytecodeSequence {

    private String type;
    private List<BytecodeSequence> argumentsPush;
    private String signature;

    public NewInvocation(String type, List<BytecodeSequence> argumentsPush, String signature) {
        this.type = type;
        this.argumentsPush = argumentsPush;
        this.signature = signature;
    }

    @Override

    public void operate(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, type);
        mv.visitInsn(DUP);
        argumentsPush.forEach((ap)->ap.operate(mv));
        //mv.visitLdcInsn("ciao");
        //mv.visitIntInsn(BIPUSH, 16);
        mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", signature, false);
    }

}
