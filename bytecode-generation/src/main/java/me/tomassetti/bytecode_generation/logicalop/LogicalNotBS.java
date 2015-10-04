package me.tomassetti.bytecode_generation.logicalop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LogicalNotBS extends BytecodeSequence {

    private BytecodeSequence valueToNegate;

    public LogicalNotBS(BytecodeSequence valueToNegate) {
        this.valueToNegate = valueToNegate;
    }

    public LogicalNotBS() {
        this.valueToNegate = null;
    }

    @Override

    public void operate(MethodVisitor mv) {
        if (valueToNegate != null) {
            valueToNegate.operate(mv);
        }
        // it is very weird that there is not a single instruction for this...
        Label l0 = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, l0);
        mv.visitInsn(Opcodes.ICONST_1);
        Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, l1);
        mv.visitLabel(l0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitLabel(l1);
    }

}
