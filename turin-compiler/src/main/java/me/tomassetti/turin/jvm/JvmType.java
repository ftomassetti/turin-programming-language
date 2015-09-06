package me.tomassetti.turin.jvm;

import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.typeusage.ArrayTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import org.objectweb.asm.Opcodes;

import java.util.Optional;

public class JvmType {

    private String signature;

    public String getSignature() {
        return signature;
    }

    public JvmType(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JvmType jvmType = (JvmType) o;

        if (!signature.equals(jvmType.signature)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "JvmType{" +
                "signature='" + signature + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return signature.hashCode();
    }

    public TypeUsage toTypeUsage() {
        Optional<PrimitiveTypeUsage> primitive = PrimitiveTypeUsage.findByJvmType(this);
        if (primitive.isPresent()) {
            return primitive.get();
        }
        if (signature.startsWith("[")) {
            return new ArrayTypeUsage(new JvmType(signature.substring(1)).toTypeUsage());
        } else if (signature.startsWith("L") && signature.endsWith(";")) {
            String typeName = signature.substring(1, signature.length() - 1);
            typeName = typeName.replaceAll("/", ".");
            return new ReferenceTypeUsage(typeName);
        } else {
            throw new UnsupportedOperationException(signature);
        }
    }

    public JvmTypeCategory typeCategory() {
        if (signature.startsWith("L")){
            return JvmTypeCategory.REFERENCE;
        }

        switch (signature) {
            case "Z":
            case "B":
            case "S":
            case "C":
            case "I":
                return JvmTypeCategory.INT;
            case"J":
                return JvmTypeCategory.LONG;
            case "F":
                return JvmTypeCategory.FLOAT;
            case "D":
                return JvmTypeCategory.DOUBLE;
            default:
                throw new UnsupportedOperationException(signature);
        }
    }

    public String getDescriptor() {
        // TODO differentiate
        return signature;
    }

    public String getInternalName() {
        if (!signature.startsWith("L")) {
            throw new UnsupportedOperationException();
        }
        return signature.substring(1, signature.length() - 1);
    }

    public int returnOpcode() {
        if (signature.equals("L")){
            return Opcodes.LRETURN;
        } else if (signature.equals("V")) {
            return Opcodes.RETURN;
        } else if (signature.equals("F")) {
            return Opcodes.FRETURN;
        } else if (signature.equals("D")) {
            return Opcodes.DRETURN;
        } else if (signature.equals("B")||signature.equals("S")||signature.equals("C")||signature.equals("I")) {
            return Opcodes.IRETURN;
        } else {
            return Opcodes.ARETURN;
        }
    }
}
