package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ASTORE;

/**
 * Created by federico on 29/08/15.
 */
public class NewInvocation extends BytecodeSequence {

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, "manga/MangaCharacter");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("ciao");
        mv.visitIntInsn(BIPUSH, 16);
        mv.visitMethodInsn(INVOKESPECIAL, "manga/MangaCharacter", "<init>", "(Ljava/lang/String;I)V", false);
    }

}
