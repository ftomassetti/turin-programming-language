package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.Resolver;

public class ReferenceTypeUsage extends TypeUsage {

    private String name;

    public ReferenceTypeUsage(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceTypeUsage that = (ReferenceTypeUsage) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ReferenceTypeUsage{" +
                "name='" + name + '\'' +

                '}';
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();

    }

    @Override
    public String jvmType(Resolver resolver) {
        TypeDefinition typeDefinition = resolver.findTypeDefinitionIn(name, this);
        return typeDefinition.jvmType();
    }

    public String getQualifiedName(Resolver resolver) {
        TypeDefinition typeDefinition = resolver.findTypeDefinitionIn(name, this);
        return typeDefinition.getQualifiedName();
    }

    @Override
    public boolean isReferenceTypeUsage() {
        return true;
    }

    @Override
    public ReferenceTypeUsage asReferenceTypeUsage() {
        return this;
    }
}
