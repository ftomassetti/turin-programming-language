package me.tomassetti.turin.compiler;

import me.tomassetti.bytecode_generation.MathOperationBS;
import me.tomassetti.bytecode_generation.RelationalOperationBS;
import me.tomassetti.jvm.JvmTypeCategory;
import me.tomassetti.turin.parser.ast.expressions.MathOperation;
import me.tomassetti.turin.parser.ast.expressions.RelationalOperation;
import org.objectweb.asm.Opcodes;

public class BytecodeUtils {

    public static RelationalOperationBS createRelationOperation(RelationalOperation.Operator operator) {
        int jumpOpcode;
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
        return new RelationalOperationBS(jumpOpcode);
    }

    public static MathOperationBS createMathOperation(JvmTypeCategory operandsType, MathOperation.Operator operator) {
        int opcode;
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
        return new MathOperationBS(opcode);
    }


}
