package me.tomassetti.turin.analysis;

import me.tomassetti.turin.ast.PropertyDefinition;
import me.tomassetti.turin.ast.PropertyReference;

/**
 * A Property can be derived by a definition in the type (PropertyDefinition) or by a reference (PropertyReference)
 */
public class Property {

    private String name;

    public String getName() {
        return name;
    }

    private Property(String name) {

        this.name = name;
    }

    public static Property fromDefinition(PropertyDefinition propertyDefinition) {
        return new Property(propertyDefinition.getName());
    }

    public static Property fromReference(PropertyReference propertyReference) {
        return new Property(propertyReference.getName());
    }

}
