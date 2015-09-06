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
                    case LONG:
                        opcode = Opcodes.LMUL;
                        break;
                    case FLOAT:
                        opcode = Opcodes.FMUL;
                        break;
                    case DOUBLE:
                        opcode = Opcodes.DMUL;
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
                    case LONG:
                        opcode = Opcodes.LADD;
                        break;
                    case FLOAT:
                        opcode = Opcodes.FADD;
                        break;
                    case DOUBLE:
                        opcode = Opcodes.DADD;
                        break;
                    default:
                        throw new UnsupportedOperationException(operandsType.name());
                }
                break;
            case SUBTRACTION:
                switch (operandsType) {
                    case INT:
                        opcode = Opcodes.ISUB;
                        break;
                    case LONG:
                        opcode = Opcodes.LSUB;
                        break;
                    case FLOAT:
                        opcode = Opcodes.FSUB;
                        break;
                    case DOUBLE:
                        opcode = Opcodes.DSUB;
                        break;
                    default:
                        throw new UnsupportedOperationException(operandsType.name());
                }
                break;
            case DIVISION:
                switch (operandsType) {
                    case INT:
                        opcode = Opcodes.IDIV;
                        break;
                    case LONG:
                        opcode = Opcodes.LDIV;
                        break;
                    case FLOAT:
                        opcode = Opcodes.FDIV;
                        break;
                    case DOUBLE:
                        opcode = Opcodes.DDIV;
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
