package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.Resolver;

import java.util.ArrayList;
import java.util.List;

public class TypeDefinition extends Node {
    private String name;
    private List<Node> members = new ArrayList<>();

    public void add(PropertyDefinition propertyDefinition){
        members.add(propertyDefinition);
        propertyDefinition.parent = this;
    }

    public TypeDefinition(String name) {
        this.name = name;
    }

    public ImmutableList<Node> getMembers() {
        return ImmutableList.copyOf(members);
    }

    public void add(PropertyReference propertyReference) {
        members.add(propertyReference);
        propertyReference.parent = this;
    }

    @Override
    public String toString() {
        return "TypeDefinition{" +
                "name='" + name + '\'' +
                ", members=" + members +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeDefinition that = (TypeDefinition) o;

        if (!members.equals(that.members)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + members.hashCode();
        return result;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(members);
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }

    public List<Property> getDirectProperties(Resolver resolver) {
        List<Property> properties = new ArrayList<>();
        for (Node member : members) {
            if (member instanceof PropertyDefinition) {
                properties.add(Property.fromDefinition((PropertyDefinition)member));
            } else if (member instanceof PropertyReference) {
                properties.add(Property.fromReference((PropertyReference) member, resolver));
            }
        }
        return properties;
    }

    public String getName() {
        return name;
    }

    public String jvmType() {
        throw new UnsupportedOperationException();
    }
}
