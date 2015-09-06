package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class FormalParameter extends Node {

    private TypeUsage type;
    private String name;

    @Override
    public String toString() {
        return "FormalParameter{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormalParameter that = (FormalParameter) o;

        if (!name.equals(that.name)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public FormalParameter(TypeUsage type, String name) {
        this.type = type;
        this.type.parent = this;
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(type);
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        return type;
    }

    public TypeUsage getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
