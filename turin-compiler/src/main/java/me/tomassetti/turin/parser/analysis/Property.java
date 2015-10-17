package me.tomassetti.turin.parser.analysis;

import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.properties.PropertyConstraint;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsageNode;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A Property can be derived by a definition in the type (PropertyDefinition) or by a reference (PropertyReference)
 */
public class Property extends Node {

    private String name;
    private TypeUsageNode typeUsage;
    private Optional<Expression> initialValue;
    private Optional<Expression> defaultValue;
    private List<PropertyConstraint> constraints;

    public List<PropertyConstraint> getConstraints() {
        return constraints;
    }

    public Optional<Expression> getInitialValue() {
        return initialValue;
    }

    public Optional<Expression> getDefaultValue() {
        return defaultValue;
    }

    private Property(String name, TypeUsageNode typeUsage, Optional<Expression> initialValue,
                     Optional<Expression> defaultValue, List<PropertyConstraint> constraints) {
        if (!JvmNameUtils.isValidJavaIdentifier(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.typeUsage = typeUsage;
        this.initialValue = initialValue;
        this.defaultValue = defaultValue;
        this.constraints = constraints;
    }

    public static Property fromDefinition(PropertyDefinition propertyDefinition) {
        Property property = new Property(propertyDefinition.getName(), propertyDefinition.getType(),
                propertyDefinition.getInitialValue(), propertyDefinition.getDefaultValue(),
                propertyDefinition.getConstraints());
        property.setParent(propertyDefinition.getParent());
        return property;
    }

    public static Property fromReference(PropertyReference propertyReference, SymbolResolver resolver) {
        Optional<PropertyDefinition> propertyDefinition = resolver.findDefinition(propertyReference);
        if (!propertyDefinition.isPresent()) {
            throw new UnsolvedSymbolException(propertyReference);
        }
        Property property = new Property(propertyReference.getName(), propertyDefinition.get().getType(),
                propertyDefinition.get().getInitialValue(), propertyDefinition.get().getDefaultValue(),
                propertyDefinition.get().getConstraints());
        property.setParent(propertyReference.getParent());
        return property;
    }

    public String getName() {
        return name;
    }

    public TypeUsageNode getTypeUsage() {
        return typeUsage;
    }

    @Override
    public TypeUsageNode calcType(SymbolResolver resolver) {
        return typeUsage;
    }

    public static String getterName(TypeUsage typeUsage, String propertyName, SymbolResolver resolver) {
        String prefix = typeUsage.sameType(PrimitiveTypeUsage.BOOLEAN, resolver) ? "is" : "get";
        String rest = propertyName.length() > 1 ? propertyName.substring(1) : "";
        return prefix + Character.toUpperCase(propertyName.charAt(0)) + rest;
    }

    public String getterName(SymbolResolver resolver) {
        return getterName(getTypeUsage(), getName(), resolver);
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

    public boolean hasDefaultValue() {
        return defaultValue.isPresent();
    }

    public boolean hasInitialValue() {
        return initialValue.isPresent();
    }
}
