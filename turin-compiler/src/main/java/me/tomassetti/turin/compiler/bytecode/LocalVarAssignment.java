package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.jvm.JvmTypeCategory;
import org.objectweb.asm.MethodVisitor;


public class LocalVarAssignment extends BytecodeSequence {

    private int assignmentTarget;
    private JvmTypeCategory jvmTypeCategory;

    /**
     * @param assignmentTarget the local variable index of the value being assigned
     * @param jvmTypeCategory
     */
    public LocalVarAssignment(int assignmentTarget, JvmTypeCategory jvmTypeCategory) {
        this.assignmentTarget = assignmentTarget;
        this.jvmTypeCategory = jvmTypeCategory;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitVarInsn(jvmTypeCategory.storeOpcode(), assignmentTarget);
    }
}
