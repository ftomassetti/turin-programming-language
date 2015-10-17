package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;

import java.util.Collections;

/**
 * Usage of a primitive type.
 *
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class PrimitiveTypeUsageNode extends TypeUsageWrapperNode {

    private String name;

    public static PrimitiveTypeUsageNode createBoolean() {
        return new PrimitiveTypeUsageNode("boolean");
    }

    public static PrimitiveTypeUsageNode createInt() {
        return new PrimitiveTypeUsageNode("int");
    }

    public static PrimitiveTypeUsageNode createFloat() {
        return new PrimitiveTypeUsageNode("float");
    }

    private PrimitiveTypeUsageNode(String name) {
        super(PrimitiveTypeUsage.getByName(name));
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
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
        return typeUsage().asPrimitiveTypeUsage().isBoolean();
    }

    @Override
    public String describe() {
        return name;
    }

}
