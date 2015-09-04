package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

/**
 * Created by federico on 28/08/15.
 */
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

    public TypeUsage getType(Resolver resolver) {
        return resolver.findDefinition(this).getType();
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
