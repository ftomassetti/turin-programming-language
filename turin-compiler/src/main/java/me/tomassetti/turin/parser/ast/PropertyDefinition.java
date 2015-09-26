package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertyDefinition extends Node {

    private String name;
    private TypeUsage type;
    private Optional<Expression> initialValue;
    private Optional<Expression> defaultValue;

    public Optional<Expression> getInitialValue() {
        return initialValue;
    }

    public Optional<Expression> getDefaultValue() {
        return defaultValue;
    }

    public PropertyDefinition(String name, TypeUsage type, Optional<Expression> initialValue, Optional<Expression> defaultValue) {
        this.name = name;
        this.type = type;
        this.initialValue = initialValue;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public TypeUsage getType() {
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
        return children;
    }

    @Override
    public String toString() {
        return "PropertyDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", initialValue=" + initialValue +
                ", defaultValue=" + defaultValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyDefinition)) return false;

        PropertyDefinition that = (PropertyDefinition) o;

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
        return result;
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
