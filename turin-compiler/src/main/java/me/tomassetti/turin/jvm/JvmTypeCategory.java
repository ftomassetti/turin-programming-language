package me.tomassetti.turin.jvm;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import org.objectweb.asm.Opcodes;

public enum JvmTypeCategory {
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    REFERENCE;

    public int storeOpcode(){
        switch (this){
            case INT:
                return Opcodes.ISTORE;
            case LONG:
                return Opcodes.LSTORE;
            case FLOAT:
                return Opcodes.FSTORE;
            case DOUBLE:
                return Opcodes.DSTORE;
            case REFERENCE:
                return Opcodes.ASTORE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public int arrayLoadOpcode(){
        switch (this){
            case INT:
                return Opcodes.IALOAD;
            case LONG:
                return Opcodes.LALOAD;
            case FLOAT:
                return Opcodes.FALOAD;
            case DOUBLE:
                return Opcodes.DALOAD;
            case REFERENCE:
                return Opcodes.AALOAD;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static JvmTypeCategory from(TypeUsage typeUsage, SymbolResolver resolver) {
        String signature = typeUsage.jvmType(resolver).getSignature();
        if (signature.startsWith("L")){
            return REFERENCE;
        }

        switch (signature) {
            case "Z":
            case "B":
            case "S":
            case "C":
            case "I":
                return INT;
            case"J":
                return LONG;
            case "F":
                return FLOAT;
            case "D":
                return DOUBLE;
            default:
                throw new UnsupportedOperationException(signature);
        }
    }
}
