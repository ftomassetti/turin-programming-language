package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;

import java.util.List;

public class ReferenceTypeUsage extends TypeUsage {

    public static final ReferenceTypeUsage STRING = new ReferenceTypeUsage("java.lang.String");

    private String name;

    public ReferenceTypeUsage(String name) {
        if (name.startsWith(".")) {
            throw new IllegalArgumentException();
        }
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
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, Resolver resolver) {
        TypeDefinition typeDefinition = resolver.findTypeDefinitionIn(name, this);
        return typeDefinition.findMethodFor(argsTypes, resolver);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();

    }

    @Override
    public JvmType jvmType(Resolver resolver) {
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
