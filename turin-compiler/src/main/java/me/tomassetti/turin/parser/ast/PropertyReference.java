package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Optional;

public class PropertyReference extends Node {
    public String getName() {
        return name;
    }

    private String name;

    public PropertyReference(String name) {
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    public TypeUsage getType(SymbolResolver resolver) {
        Optional<PropertyDefinition> propertyDefinition = resolver.findDefinition(this);
        if (propertyDefinition.isPresent()) {
            return propertyDefinition.get().getType();
        } else {
            throw new UnsolvedSymbolException(this);
        }
    }

    @Override
    public String toString() {
        return "PropertyReference{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyReference that = (PropertyReference) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
