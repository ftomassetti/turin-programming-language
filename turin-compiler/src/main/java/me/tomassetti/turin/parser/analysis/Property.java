package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;
import java.util.Optional;

/**
 * A Property can be derived by a definition in the type (PropertyDefinition) or by a reference (PropertyReference)
 */
public class Property extends Node {

    private String name;
    private TypeUsage typeUsage;
    private Optional<Expression> initialValue;
    private Optional<Expression> defaultValue;

    public Optional<Expression> getInitialValue() {
        return initialValue;
    }

    public Optional<Expression> getDefaultValue() {
        return defaultValue;
    }

    private Property(String name, TypeUsage typeUsage, Optional<Expression> initialValue, Optional<Expression> defaultValue) {
        if (!JvmNameUtils.isValidJavaIdentifier(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.typeUsage = typeUsage;
        this.initialValue = initialValue;
        this.defaultValue = defaultValue;
    }

    public static Property fromDefinition(PropertyDefinition propertyDefinition) {
        return new Property(propertyDefinition.getName(), propertyDefinition.getType(), propertyDefinition.getInitialValue(), propertyDefinition.getDefaultValue());
    }

    public static Property fromReference(PropertyReference propertyReference, SymbolResolver resolver) {
        Optional<PropertyDefinition> propertyDefinition = resolver.findDefinition(propertyReference);
        if (!propertyDefinition.isPresent()) {
            throw new UnsolvedSymbolException(propertyReference);
        }
        return new Property(propertyReference.getName(), propertyDefinition.get().getType(),
                propertyDefinition.get().getInitialValue(), propertyDefinition.get().getDefaultValue());
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

    public boolean hasDefaultValue() {
        return defaultValue.isPresent();
    }

    public boolean hasInitialValue() {
        return initialValue.isPresent();
    }
}
