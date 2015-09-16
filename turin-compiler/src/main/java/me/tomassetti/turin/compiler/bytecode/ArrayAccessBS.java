package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.jvm.JvmTypeCategory;
import org.objectweb.asm.MethodVisitor;

public class ArrayAccessBS extends BytecodeSequence {

    private JvmTypeCategory jvmTypeCategory;

    public ArrayAccessBS(JvmTypeCategory jvmTypeCategory) {
        this.jvmTypeCategory = jvmTypeCategory;
    }

    @Override
    public void operate(MethodVisitor mv) {
       mv.visitInsn(jvmTypeCategory.arrayLoadOpcode());
    }

}
