package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;

/**
 * A Property can be derived by a definition in the type (PropertyDefinition) or by a reference (PropertyReference)
 */
public class Property extends Node {

    private String name;
    private TypeUsage typeUsage;

    public String getName() {
        return name;
    }

    public TypeUsage getTypeUsage() {
        return typeUsage;
    }

    private Property(String name, TypeUsage typeUsage) {
        if (!JvmNameUtils.isValidJavaIdentifier(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.typeUsage = typeUsage;
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        return typeUsage;
    }

    public static Property fromDefinition(PropertyDefinition propertyDefinition) {
        return new Property(propertyDefinition.getName(), propertyDefinition.getType());
    }

    public static Property fromReference(PropertyReference propertyReference, Resolver resolver) {
        PropertyDefinition propertyDefinition = resolver.findDefinition(propertyReference);
        return new Property(propertyReference.getName(), propertyDefinition.getType());
    }

    public String getterName() {
        String prefix = getTypeUsage().equals(PrimitiveTypeUsage.BOOLEAN) ? "is" : "get";
        String rest = getName().length() > 1 ? getName().substring(1) : "";
        return prefix + Character.toUpperCase(getName().charAt(0)) + rest;
    }

    public String setterName() {
        String rest = getName().length() > 1 ? getName().substring(1) : "";
        return "set" + Character.toUpperCase(getName().charAt(0)) + rest;
    }

    public String fieldName() {
        return name;
    }

    @Override
    public Iterable<Node> getChildren() {
        // this is intended
        return Collections.emptyList();
    }

}
