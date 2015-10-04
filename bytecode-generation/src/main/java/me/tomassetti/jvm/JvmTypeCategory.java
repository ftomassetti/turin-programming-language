package me.tomassetti.jvm;

import org.objectweb.asm.Opcodes;

public enum JvmTypeCategory {
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    REFERENCE,
    ARRAY;

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
            case ARRAY:
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

}
