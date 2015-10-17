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

    public static PrimitiveTypeUsageNode BOOLEAN = new PrimitiveTypeUsageNode("boolean");
    public static PrimitiveTypeUsageNode CHAR = new PrimitiveTypeUsageNode("char");
    public static PrimitiveTypeUsageNode LONG = new PrimitiveTypeUsageNode("long");
    public static PrimitiveTypeUsageNode INT = new PrimitiveTypeUsageNode("int");
    public static PrimitiveTypeUsageNode SHORT = new PrimitiveTypeUsageNode("short");
    public static PrimitiveTypeUsageNode BYTE = new PrimitiveTypeUsageNode("byte");
    public static PrimitiveTypeUsageNode DOUBLE = new PrimitiveTypeUsageNode("double");
    public static PrimitiveTypeUsageNode FLOAT = new PrimitiveTypeUsageNode("float");
    public static List<PrimitiveTypeUsageNode> ALL = ImmutableList.of(BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE);

    private PrimitiveTypeUsageNode(String name) {
        super(PrimitiveTypeUsage.getByName(name));
        this.name = name;
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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveTypeUsageNode that = (PrimitiveTypeUsageNode) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
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
        PrimitiveTypeUsageNode copy = new PrimitiveTypeUsageNode(this.name);
        copy.parent = this.parent;
        return copy;
    }

    public boolean isLong() {
        return typeUsage().asPrimitiveTypeUsage().isLong();
    }

    public boolean isFloat() {
        return typeUsage().asPrimitiveTypeUsage().isFloat();
    }

    public boolean isDouble() {
        return typeUsage().asPrimitiveTypeUsage().isDouble();
    }

    public boolean isStoredInInt() {
        return typeUsage().asPrimitiveTypeUsage().isStoredInInt();
    }

    public boolean isInt() {
        return typeUsage().asPrimitiveTypeUsage().isInt();
    }

    public boolean isShort() {
        return typeUsage().asPrimitiveTypeUsage().isShort();
    }

    public boolean isChar() {
        return typeUsage().asPrimitiveTypeUsage().isChar();
    }

    public boolean isByte() {
        return typeUsage().asPrimitiveTypeUsage().isByte();
    }

    public boolean isBoolean() {
        return this == PrimitiveTypeUsageNode.BOOLEAN;
    }

    @Override
    public String describe() {
        return name;
    }

}
