package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.parser.ast.expressions.RelationalOperation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RelationalOperationBytecode extends BytecodeSequence {

    private int jumpOpcode;

    public RelationalOperationBytecode(RelationalOperation.Operator operator) {
        switch (operator) {
            case EQUAL:
                jumpOpcode = Opcodes.IF_ICMPNE;
                break;
            case DIFFERENT:
                jumpOpcode = Opcodes.IF_ICMPEQ;
                break;
            case LESS:
                jumpOpcode = Opcodes.IF_ICMPGE;
                break;
            case LESSEQ:
                jumpOpcode = Opcodes.IF_ICMPGT;
                break;
            case MORE:
                jumpOpcode = Opcodes.IF_ICMPLE;
                break;
            case MOREEQ:
                jumpOpcode = Opcodes.IF_ICMPLT;
                break;
            default:
                throw new UnsupportedOperationException(operator.name());
        }
    }

    @Override
    public void operate(MethodVisitor mv) {
        Label l0 = new Label();
        mv.visitJumpInsn(jumpOpcode, l0);
        mv.visitInsn(Opcodes.ICONST_1);
        Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, l1);
        mv.visitLabel(l0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitLabel(l1);
    }
}
