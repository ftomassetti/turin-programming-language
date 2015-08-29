package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASTORE;

/**
 * Created by federico on 29/08/15.
 */
public class Assignment extends BytecodeSequence {

    private int assignmentTarget;
    private JvmTypeCategory jvmTypeCategory;

    public Assignment(int assignmentTarget, JvmTypeCategory jvmTypeCategory) {
        this.assignmentTarget = assignmentTarget;
        this.jvmTypeCategory = jvmTypeCategory;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitVarInsn(jvmTypeCategory.storeOpcode(), assignmentTarget);
    }
}
