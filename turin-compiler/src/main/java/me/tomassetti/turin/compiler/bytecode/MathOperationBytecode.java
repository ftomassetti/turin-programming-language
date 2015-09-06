package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.jvm.JvmTypeCategory;
import me.tomassetti.turin.parser.ast.expressions.MathOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MathOperationBytecode extends BytecodeSequence {

    private int opcode;

    public MathOperationBytecode(JvmTypeCategory operandsType, MathOperation.Operator operator) {
        switch (operator) {
            case MULTIPLICATION:
                switch (operandsType) {
                    case INT:
                        opcode = Opcodes.IMUL;
                        break;
                    default:
                        throw new UnsupportedOperationException(operandsType.name());
                }
                break;
            case SUM:
                switch (operandsType) {
                    case INT:
                        opcode = Opcodes.IADD;
                        break;
                    default:
                        throw new UnsupportedOperationException(operandsType.name());
                }
                break;
            default:
                throw new UnsupportedOperationException(operator.name());
        }
    }

    @Override
    public void operate(MethodVisitor mv) {
        mv.visitInsn(this.opcode);
    }
}
