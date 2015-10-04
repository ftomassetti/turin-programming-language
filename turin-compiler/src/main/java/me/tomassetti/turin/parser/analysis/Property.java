package me.tomassetti.turin.parser.analysis;

import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.PropertyConstraint;
import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A Property can be derived by a definition in the type (PropertyDefinition) or by a reference (PropertyReference)
 */
public class Property extends Node {

    private String name;
    private TypeUsage typeUsage;
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

    private Property(String name, TypeUsage typeUsage, Optional<Expression> initialValue,
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
        return new Property(propertyDefinition.getName(), propertyDefinition.getType(),
                propertyDefinition.getInitialValue(), propertyDefinition.getDefaultValue(),
                propertyDefinition.getConstraints());
    }

    public static Property fromReference(PropertyReference propertyReference, SymbolResolver resolver) {
        Optional<PropertyDefinition> propertyDefinition = resolver.findDefinition(propertyReference);
        if (!propertyDefinition.isPresent()) {
            throw new UnsolvedSymbolException(propertyReference);
        }
        return new Property(propertyReference.getName(), propertyDefinition.get().getType(),
                propertyDefinition.get().getInitialValue(), propertyDefinition.get().getDefaultValue(),
                propertyDefinition.get().getConstraints());
    }

    public String getName() {
        return name;
    }

    public TypeUsage getTypeUsage() {
        return typeUsage;
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        return typeUsage;
    }

    public static String getterName(TypeUsage typeUsage, String propertyName) {
        String prefix = typeUsage.equals(PrimitiveTypeUsage.BOOLEAN) ? "is" : "get";
        String rest = propertyName.length() > 1 ? propertyName.substring(1) : "";
        return prefix + Character.toUpperCase(propertyName.charAt(0)) + rest;
    }

    public String getterName() {
        return getterName(getTypeUsage(), getName());
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
