package me.tomassetti.turin.parser.ast.properties;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertyDefinition extends Node implements Symbol {

    private String name;
    private TypeUsageNode type;
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

    public PropertyDefinition(String name, TypeUsageNode type, Optional<Expression> initialValue, Optional<Expression> defaultValue,
                              List<PropertyConstraint> constraints) {
        this.name = name;
        this.type = type;
        this.type.setParent(this);
        this.initialValue = initialValue;
        if (initialValue.isPresent()) {
            initialValue.get().setParent(this);
        }
        this.defaultValue = defaultValue;
        if (defaultValue.isPresent()) {
            defaultValue.get().setParent(this);
        }
        this.constraints = constraints;
        this.constraints.forEach((c)->c.setParent(PropertyDefinition.this));
    }

    public String getName() {
        return name;
    }

    public TypeUsageNode getType() {
        return type;
    }

    @Override
    public Iterable<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        children.add(type);
        if (initialValue.isPresent()) {
            children.add(initialValue.get());
        }
        if (defaultValue.isPresent()) {
            children.add(defaultValue.get());
        }
        children.addAll(constraints);
        return children;
    }

    @Override
    public String toString() {
        return "PropertyDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", initialValue=" + initialValue +
                ", defaultValue=" + defaultValue +
                ", constraints=" + constraints +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyDefinition)) return false;

        PropertyDefinition that = (PropertyDefinition) o;

        if (!constraints.equals(that.constraints)) return false;
        if (!defaultValue.equals(that.defaultValue)) return false;
        if (!initialValue.equals(that.initialValue)) return false;
        if (!name.equals(that.name)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + initialValue.hashCode();
        result = 31 * result + defaultValue.hashCode();
        result = 31 * result + constraints.hashCode();
        return result;
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
