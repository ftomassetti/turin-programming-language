package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.analysis.Property;
import me.tomassetti.turin.analysis.Resolver;

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
