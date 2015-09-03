package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class PropertyDefinition extends Node {

    private String name;
    private TypeUsage type;

    public PropertyDefinition(String name, TypeUsage type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TypeUsage getType() {
        return type;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public String toString() {
        return "PropertyDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyDefinition that = (PropertyDefinition) o;

        if (!name.equals(that.name)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
