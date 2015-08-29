package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.TypeUsage;

/**
 * A Property can be derived by a definition in the type (PropertyDefinition) or by a reference (PropertyReference)
 */
public class Property {

    private String name;
    private TypeUsage typeUsage;

    public String getName() {
        return name;
    }

    public TypeUsage getTypeUsage() {
        return typeUsage;
    }

    private Property(String name, TypeUsage typeUsage) {
        this.name = name;
        this.typeUsage = typeUsage;
    }

    public static Property fromDefinition(PropertyDefinition propertyDefinition) {
        return new Property(propertyDefinition.getName(), propertyDefinition.getType());
    }

    public static Property fromReference(PropertyReference propertyReference, Resolver resolver) {
        PropertyDefinition propertyDefinition = resolver.findDefinition(propertyReference);
        return new Property(propertyReference.getName(), propertyDefinition.getType());
    }

}
