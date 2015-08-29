package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.parser.ast.TypeUsage;
import org.objectweb.asm.Opcodes;

/**
 * Created by federico on 29/08/15.
 */
public enum JvmTypeCategory {
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    REFERENCE;

    public int storeOpcode(){
        switch (this){
            case INT:
                return Opcodes.ILOAD;
            case LONG:
                return Opcodes.LLOAD;
            case FLOAT:
                return Opcodes.FLOAD;
            case DOUBLE:
                return Opcodes.DLOAD;
            case REFERENCE:
                return Opcodes.ALOAD;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static JvmTypeCategory from(TypeUsage typeUsage) {
        throw new UnsupportedOperationException();
    }
}
