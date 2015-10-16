package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Usage of a primitive type.
 *
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class PrimitiveTypeUsageNode extends TypeUsageWrapperNode {

    private String name;
    private JvmType jvmType;
    private List<PrimitiveTypeUsageNode> promotionsTypes;

    public static PrimitiveTypeUsageNode BOOLEAN = new PrimitiveTypeUsageNode("boolean", new JvmType("Z"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Boolean.class)));
    public static PrimitiveTypeUsageNode CHAR = new PrimitiveTypeUsageNode("char",  new JvmType("C"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Character.class)));
    public static PrimitiveTypeUsageNode LONG = new PrimitiveTypeUsageNode("long",  new JvmType("J"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Long.class)));
    public static PrimitiveTypeUsageNode INT = new PrimitiveTypeUsageNode("int",  new JvmType("I"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Integer.class)),
            ImmutableList.of(LONG));
    public static PrimitiveTypeUsageNode SHORT = new PrimitiveTypeUsageNode("short",  new JvmType("S"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Short.class)),
            ImmutableList.of(INT, LONG));
    public static PrimitiveTypeUsageNode BYTE = new PrimitiveTypeUsageNode("byte",  new JvmType("B"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Byte.class)),
            ImmutableList.of(SHORT, INT, LONG));
    public static PrimitiveTypeUsageNode DOUBLE = new PrimitiveTypeUsageNode("double",  new JvmType("D"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Double.class)));
    public static PrimitiveTypeUsageNode FLOAT = new PrimitiveTypeUsageNode("float",  new JvmType("F"),
            new ReferenceTypeUsage(ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Float.class)),
            ImmutableList.of(DOUBLE));
    public static List<PrimitiveTypeUsageNode> ALL = ImmutableList.of(BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE);

    public static Optional<PrimitiveTypeUsageNode> findByJvmType(JvmType jvmType) {
        for (PrimitiveTypeUsageNode primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.jvmType.equals(jvmType)) {
                return Optional.of(primitiveTypeUsage);
            }
        }
        return Optional.empty();
    }

    public TypeUsage getBoxType() {
        return boxType;
    }

    private PrimitiveTypeUsageNode(String name, JvmType jvmType, TypeUsage boxType, List<PrimitiveTypeUsageNode> promotionsTypes) {
        super(PrimitiveTypeUsage.getByName(name));
        this.name = name;
        this.jvmType = jvmType;
        this.boxType = boxType;
        this.promotionsTypes = promotionsTypes;
    }

    private PrimitiveTypeUsageNode(String name, JvmType jvmType, TypeUsage boxType) {
        this(name, jvmType, boxType, Collections.emptyList());
    }

    private TypeUsage boxType;

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
    public static TypeUsageNode getByName(String name) {
        for (PrimitiveTypeUsageNode primitiveTypeUsage : ALL) {
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

        PrimitiveTypeUsageNode that = (PrimitiveTypeUsageNode) o;

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
        for (PrimitiveTypeUsageNode primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.name.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TypeUsageNode copy() {
        PrimitiveTypeUsageNode copy = new PrimitiveTypeUsageNode(this.name, this.jvmType, this.boxType, promotionsTypes);
        copy.parent = this.parent;
        return copy;
    }

    public boolean isLong() {
        return this == PrimitiveTypeUsageNode.LONG;
    }

    public boolean isFloat() {
        return this == PrimitiveTypeUsageNode.FLOAT;
    }

    public boolean isDouble() {
        return this == PrimitiveTypeUsageNode.DOUBLE;
    }

    public boolean isStoredInInt() {
        return jvmType.isStoredInInt();
    }

    public boolean isInt() {
        return this == INT;
    }

    public boolean isShort() {
        return this == PrimitiveTypeUsageNode.SHORT;
    }

    public boolean isChar() {
        return this == PrimitiveTypeUsageNode.CHAR;
    }

    public boolean isByte() {
        return this == PrimitiveTypeUsageNode.BYTE;
    }

    public boolean isBoolean() {
        return this == PrimitiveTypeUsageNode.BOOLEAN;
    }

    @Override
    public String describe() {
        return name;
    }

}
