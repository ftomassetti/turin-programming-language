package me.tomassetti.java.symbol_solver.type_usage;

import me.tomassetti.java.symbol_solver.JavaTypeResolver;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.jvm.JvmTypeCategory;

import java.util.Optional;

/**
 * A TypeUsage is the concrete usage of a type int the code.
 * For example it can be a type definition with generic type parameter specified.
 */
public abstract class JavaTypeUsage {

    public static JavaTypeUsage fromJvmType(JvmType jvmType) {
        Optional<PrimitiveTypeUsage> primitive = PrimitiveTypeUsage.findByJvmType(jvmType);
        if (primitive.isPresent()) {
            return primitive.get();
        }
        String signature = jvmType.getSignature();
        if (signature.startsWith("[")) {
            JvmType componentType = new JvmType(signature.substring(1));
            return new ArrayTypeUsage(fromJvmType(componentType));
        } else if (signature.startsWith("L") && signature.endsWith(";")) {
            String typeName = signature.substring(1, signature.length() - 1);
            typeName = typeName.replaceAll("/", ".");
            return new ReferenceTypeUsage(typeName);
        } else {
            throw new UnsupportedOperationException(signature);
        }
    }

    public JvmTypeCategory toJvmTypeCategory(JavaTypeResolver resolver) {
        return this.jvmType(resolver).typeCategory();
    }

    public abstract JvmType jvmType(JavaTypeResolver resolver);

    public boolean isReferenceTypeUsage() {
        return false;
    }

    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

    public ArrayTypeUsage asArrayTypeUsage() {
        throw new UnsupportedOperationException();
    }

    public boolean isArray() {
        return this instanceof ArrayTypeUsage;
    }

    public boolean isPrimitive() {
        return this instanceof PrimitiveTypeUsage;
    }

    public boolean isReference() {
        return this instanceof ReferenceTypeUsage;
    }

    public PrimitiveTypeUsage asPrimitiveTypeUsage() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public boolean isVoid() {
        return false;
    }
}
