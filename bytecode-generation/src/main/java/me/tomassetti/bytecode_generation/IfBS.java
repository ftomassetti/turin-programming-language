package me.tomassetti.bytecode_generation;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.List;

public class IfBS extends BytecodeSequence {

    private BytecodeSequence pushIfCondition;
    private BytecodeSequence ifBody;
    private BytecodeSequence elseBody;
    // they just push the values
    private List<BytecodeSequence> elifConditions;
    private List<BytecodeSequence> elifBodys;

    public IfBS(BytecodeSequence pushIfCondition, BytecodeSequence ifBody) {
        this(pushIfCondition, ifBody, null);
    }

    public IfBS(BytecodeSequence pushIfCondition, BytecodeSequence ifBody, BytecodeSequence elseBody) {
        this(pushIfCondition, ifBody, Collections.emptyList(), Collections.emptyList(), elseBody);
    }

    public IfBS(BytecodeSequence pushIfCondition, BytecodeSequence ifBody, List<BytecodeSequence> elifConditions, List<BytecodeSequence> elifBodys, BytecodeSequence elseBody) {
        if (elifConditions.size() != elifBodys.size()) {
            throw new IllegalArgumentException();
        }
        this.pushIfCondition = pushIfCondition;
        this.ifBody = ifBody;
        this.elseBody = elseBody;
        this.elifConditions = elifConditions;
        this.elifBodys = elifBodys;
    }

    public IfBS(BytecodeSequence pushIfCondition, BytecodeSequence ifBody, List<BytecodeSequence> elifConditions, List<BytecodeSequence> elifBodys) {
        if (elifConditions.size() != elifBodys.size()) {
            throw new IllegalArgumentException();
        }
        this.pushIfCondition = pushIfCondition;
        this.ifBody = ifBody;
        this.elifConditions = elifConditions;
        this.elifBodys = elifBodys;
    }

    @Override
    public void operate(MethodVisitor mv) {
        pushIfCondition.operate(mv);
        Label end = new Label();

        // if equal to zero (if false) jump away
        Label ifFailed = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, ifFailed);

        // if branch
        ifBody.operate(mv);
        mv.visitJumpInsn(Opcodes.GOTO, end);

        // then branch
        mv.visitLabel(ifFailed);

        for (int i=0; i<elifConditions.size();i++){
            Label thisElifSkipped = new Label();
            elifConditions.get(i).operate(mv);
            mv.visitJumpInsn(Opcodes.IFEQ, thisElifSkipped);

            // enter the elig
            elifBodys.get(i).operate(mv);
            mv.visitJumpInsn(Opcodes.GOTO, end);

            mv.visitLabel(thisElifSkipped);
        }

        // else branch
        if (elseBody != null) {
            elseBody.operate(mv);
        }

        // enf of the statement
        mv.visitLabel(end);
    }

}
