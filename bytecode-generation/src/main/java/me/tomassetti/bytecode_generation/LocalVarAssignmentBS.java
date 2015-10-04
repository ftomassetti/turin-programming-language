package me.tomassetti.bytecode_generation;

import me.tomassetti.jvm.JvmTypeCategory;
import org.objectweb.asm.MethodVisitor;

public class LocalVarAssignmentBS extends BytecodeSequence {

    private int assignmentTarget;
    private JvmTypeCategory jvmTypeCategory;

    /**
     * @param assignmentTarget the local variable index of the value being assigned
     * @param jvmTypeCategory
     */
    public LocalVarAssignmentBS(int assignmentTarget, JvmTypeCategory jvmTypeCategory) {
        if (assignmentTarget < 0) {
            throw new IllegalArgumentException();
        }
        this.assignmentTarget = assignmentTarget;
        this.jvmTypeCategory = jvmTypeCategory;
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitVarInsn(jvmTypeCategory.storeOpcode(), assignmentTarget);
    }
}
