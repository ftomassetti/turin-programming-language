package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.ArrayTypeUsage;
import me.tomassetti.turin.parser.ast.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.TypeUsage;

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
}
