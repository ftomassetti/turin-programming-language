package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Usage of a primitive type.
 *
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class PrimitiveTypeUsage extends TypeUsage {

    private String name;
    private JvmType jvmType;

    public static PrimitiveTypeUsage BOOLEAN = new PrimitiveTypeUsage("boolean", new JvmType("Z"),
            new ReferenceTypeUsage(Boolean.class.getCanonicalName()));
    public static PrimitiveTypeUsage CHAR = new PrimitiveTypeUsage("char",  new JvmType("C"),
            new ReferenceTypeUsage(Character.class.getCanonicalName()));
    public static PrimitiveTypeUsage BYTE = new PrimitiveTypeUsage("byte",  new JvmType("B"),
            new ReferenceTypeUsage(Byte.class.getCanonicalName()));
    public static PrimitiveTypeUsage SHORT = new PrimitiveTypeUsage("short",  new JvmType("S"),
            new ReferenceTypeUsage(Short.class.getCanonicalName()));
    public static PrimitiveTypeUsage INT = new PrimitiveTypeUsage("int",  new JvmType("I"),
            new ReferenceTypeUsage(Integer.class.getCanonicalName()));
    public static PrimitiveTypeUsage LONG = new PrimitiveTypeUsage("long",  new JvmType("J"),
            new ReferenceTypeUsage(Long.class.getCanonicalName()));
    public static PrimitiveTypeUsage FLOAT = new PrimitiveTypeUsage("float",  new JvmType("F"),
            new ReferenceTypeUsage(Float.class.getCanonicalName()));
    public static PrimitiveTypeUsage DOUBLE = new PrimitiveTypeUsage("double",  new JvmType("D"),
            new ReferenceTypeUsage(Double.class.getCanonicalName()));
    public static List<PrimitiveTypeUsage> ALL = ImmutableList.of(BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE);

    @Override
    public boolean canBeAssignedTo(TypeUsage other, SymbolResolver resolver) {
        if (other.equals(boxType) || other.equals(ReferenceTypeUsage.OBJECT)) {
            return true;
        }
        if (!other.isPrimitive()) {
            return false;
        }
        // TODO consider promotions
        return jvmType(resolver).equals(other.jvmType(resolver));
    }

    public static Optional<PrimitiveTypeUsage> findByJvmType(JvmType jvmType) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.jvmType.equals(jvmType)) {
                return Optional.of(primitiveTypeUsage);
            }
        }
        return Optional.empty();
    }

    private PrimitiveTypeUsage(String name, JvmType jvmType, TypeUsage boxType) {
        this.name = name;
        this.jvmType = jvmType;
        this.boxType = boxType;
    }

    private TypeUsage boxType;

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        return jvmType;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    /**
     * In Turin all type names are capitalized, this is true also for primitive types.
     */
    public String turinName() {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * It accepts both Java name (lower case) or Turin name (capitalized).
     */
    public static TypeUsage getByName(String name) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.turinName().equals(name) || primitiveTypeUsage.name.equals(name)) {
                return primitiveTypeUsage;
            }
        }
        throw new IllegalArgumentException(name);
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
        return (this == BYTE || this == SHORT || this == INT);
    }

    public boolean isInt() {
        return this == INT;
    }
}
