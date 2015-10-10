package me.tomassetti.java.symbol_solver.type_usage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.java.symbol_solver.JavaTypeResolver;
import me.tomassetti.jvm.JvmType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Usage of a primitive type.
 *
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class PrimitiveTypeUsage extends JavaTypeUsage {

    private String name;
    private JvmType jvmType;
    private List<PrimitiveTypeUsage> promotionsTypes;

    public static PrimitiveTypeUsage BOOLEAN = new PrimitiveTypeUsage("boolean", new JvmType("Z"),
            new ReferenceTypeUsage(Boolean.class.getCanonicalName()));
    public static PrimitiveTypeUsage CHAR = new PrimitiveTypeUsage("char",  new JvmType("C"),
            new ReferenceTypeUsage(Character.class.getCanonicalName()));
    public static PrimitiveTypeUsage LONG = new PrimitiveTypeUsage("long",  new JvmType("J"),
            new ReferenceTypeUsage(Long.class.getCanonicalName()));
    public static PrimitiveTypeUsage INT = new PrimitiveTypeUsage("int",  new JvmType("I"),
            new ReferenceTypeUsage(Integer.class.getCanonicalName()),
            ImmutableList.of(LONG));
    public static PrimitiveTypeUsage SHORT = new PrimitiveTypeUsage("short",  new JvmType("S"),
            new ReferenceTypeUsage(Short.class.getCanonicalName()),
            ImmutableList.of(INT, LONG));
    public static PrimitiveTypeUsage BYTE = new PrimitiveTypeUsage("byte",  new JvmType("B"),
            new ReferenceTypeUsage(Byte.class.getCanonicalName()),
            ImmutableList.of(SHORT, INT, LONG));
    public static PrimitiveTypeUsage DOUBLE = new PrimitiveTypeUsage("double",  new JvmType("D"),
            new ReferenceTypeUsage(Double.class.getCanonicalName()));
    public static PrimitiveTypeUsage FLOAT = new PrimitiveTypeUsage("float",  new JvmType("F"),
            new ReferenceTypeUsage(Float.class.getCanonicalName()),
            ImmutableList.of(DOUBLE));
    public static List<PrimitiveTypeUsage> ALL = ImmutableList.of(BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE);


    public static Optional<PrimitiveTypeUsage> findByJvmType(JvmType jvmType) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.jvmType.equals(jvmType)) {
                return Optional.of(primitiveTypeUsage);
            }
        }
        return Optional.empty();
    }

    public JavaTypeUsage getBoxType() {
        return boxType;
    }

    private PrimitiveTypeUsage(String name, JvmType jvmType, JavaTypeUsage boxType, List<PrimitiveTypeUsage> promotionsTypes) {
        this.name = name;
        this.jvmType = jvmType;
        this.boxType = boxType;
        this.promotionsTypes = promotionsTypes;
    }

    private PrimitiveTypeUsage(String name, JvmType jvmType, JavaTypeUsage boxType) {
        this(name, jvmType, boxType, Collections.emptyList());
    }

    private JavaTypeUsage boxType;

    @Override
    public JvmType jvmType(JavaTypeResolver resolver) {
        return jvmType;
    }

    @Override
    public String toString() {
        return "PrimitiveTypeUsage{" +
                "name='" + name + '\'' +
                ", jvmType=" + jvmType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveTypeUsage that = (PrimitiveTypeUsage) o;

        if (!jvmType.equals(that.jvmType)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + jvmType.hashCode();
        return result;
    }

    public static boolean isPrimitiveTypeName(String typeName) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.name.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    public static JavaTypeUsage getByName(String name) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.name.equals(name)) {
                return primitiveTypeUsage;
            }
        }
        throw new IllegalArgumentException(name);
    }

    @Override
    public PrimitiveTypeUsage asPrimitiveTypeUsage() {
        return this;
    }


    public boolean isLong() {
        return this == PrimitiveTypeUsage.LONG;
    }

    public boolean isFloat() {
        return this == PrimitiveTypeUsage.FLOAT;
    }

    public boolean isDouble() {
        return this == PrimitiveTypeUsage.DOUBLE;
    }

    public boolean isStoredInInt() {
        return jvmType.isStoredInInt();
    }

    public boolean isInt() {
        return this == INT;
    }

    public boolean isShort() {
        return this == PrimitiveTypeUsage.SHORT;
    }

    public boolean isChar() {
        return this == PrimitiveTypeUsage.CHAR;
    }

    public boolean isByte() {
        return this == PrimitiveTypeUsage.BYTE;
    }

    public boolean isBoolean() {
        return this == PrimitiveTypeUsage.BOOLEAN;
    }
}
