package me.tomassetti.turin.typesystem;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Usage of a primitive type.
 *
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class PrimitiveTypeUsage implements TypeUsage {

    private String name;
    private JvmType jvmType;
    private List<PrimitiveTypeUsage> promotionsTypes;

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        return this;
    }

    public static PrimitiveTypeUsage BOOLEAN = new PrimitiveTypeUsage("boolean", new JvmType("Z"),
            Boolean.class);
    public static PrimitiveTypeUsage CHAR = new PrimitiveTypeUsage("char",  new JvmType("C"),
            Character.class);
    public static PrimitiveTypeUsage LONG = new PrimitiveTypeUsage("long",  new JvmType("J"),
            Long.class);
    public static PrimitiveTypeUsage INT = new PrimitiveTypeUsage("int",  new JvmType("I"),
            Integer.class,
            ImmutableList.of(LONG));
    public static PrimitiveTypeUsage SHORT = new PrimitiveTypeUsage("short",  new JvmType("S"),
            Short.class,
            ImmutableList.of(INT, LONG));
    public static PrimitiveTypeUsage BYTE = new PrimitiveTypeUsage("byte",  new JvmType("B"),
            Byte.class,
            ImmutableList.of(SHORT, INT, LONG));
    public static PrimitiveTypeUsage DOUBLE = new PrimitiveTypeUsage("double",  new JvmType("D"),
            Double.class);
    public static PrimitiveTypeUsage FLOAT = new PrimitiveTypeUsage("float",  new JvmType("F"),
            Float.class,
            ImmutableList.of(DOUBLE));
    public static List<PrimitiveTypeUsage> ALL = ImmutableList.of(BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE);

    private boolean isBoxType(TypeUsage other) {
        return other.isReferenceTypeUsage() && other.asReferenceTypeUsage().getQualifiedName().equals(boxTypeClazz.getCanonicalName());
    }

    private boolean isObject(TypeUsage other) {
        return other.isReferenceTypeUsage() && other.asReferenceTypeUsage().getQualifiedName().equals(Object.class.getCanonicalName());
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage other) {
        if (isBoxType(other) || isObject(other)) {
            return true;
        }
        if (!other.isPrimitive()) {
            return false;
        }
        if (promotionsTypes.contains(other)) {
            return true;
        }
        return jvmType().equals(other.jvmType());
    }

    public static Optional<PrimitiveTypeUsage> findByJvmType(JvmType jvmType) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.jvmType.equals(jvmType)) {
                return Optional.of(primitiveTypeUsage);
            }
        }
        return Optional.empty();
    }

    public TypeUsage getBoxType(SymbolResolver resolver) {
        return new ReferenceTypeUsage(new ReflectionTypeDefinitionFactory().getTypeDefinition(boxTypeClazz, resolver));
    }

    private PrimitiveTypeUsage(String name, JvmType jvmType, Class<?> boxTypeClazz, List<PrimitiveTypeUsage> promotionsTypes) {
        this.name = name;
        this.jvmType = jvmType;
        this.boxTypeClazz = boxTypeClazz;
        this.promotionsTypes = promotionsTypes;
    }

    private PrimitiveTypeUsage(String name, JvmType jvmType, Class<?> boxTypeClazz) {
        this(name, jvmType, boxTypeClazz, Collections.emptyList());
    }

    private Class<?> boxTypeClazz;

    @Override
    public JvmType jvmType() {
        return jvmType;
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, boolean staticContext) {
        throw new UnsupportedOperationException();
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
    public static PrimitiveTypeUsage getByName(String name) {
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

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsageNode returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        throw new UnsupportedOperationException();
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

    public String getName() {
        return name;
    }

    @Override
    public String describe() {
        return name;
    }

    @Override
    public boolean sameType(TypeUsage other) {
        if (!other.isPrimitive()) {
            return false;
        }
        return getName().equals(other.asPrimitiveTypeUsage().getName());
    }

}
