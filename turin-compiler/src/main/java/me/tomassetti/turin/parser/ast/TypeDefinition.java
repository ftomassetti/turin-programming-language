package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        String contextName = contextName();
        if (contextName.isEmpty()) {
            return name;
        } else {
            return contextName + "." + name;
        }
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

    public JvmType jvmType() {
        return new JvmType("L" + getQualifiedName().replaceAll("\\.", "/") + ";");
    }

    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public String resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams) {
        // We generate one single constructor so call that ibe
        List<String> paramSignatures = getDirectProperties(resolver).stream()
                .map((p) -> p.getTypeUsage()
                .jvmType(resolver).getSignature())
                .collect(Collectors.toList());
        return "(" + String.join("", paramSignatures) + ")V";
    }

    public TypeUsage getField(String fieldName, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
