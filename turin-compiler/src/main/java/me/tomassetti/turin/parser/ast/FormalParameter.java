package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FormalParameter extends Node {

    private TypeUsage type;
    private String name;
    private Optional<Expression> defaultValue;

    @Override
    public String toString() {
        return "FormalParameter{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", defaultValue=" + defaultValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormalParameter)) return false;

        FormalParameter that = (FormalParameter) o;

        if (!defaultValue.equals(that.defaultValue)) return false;
        if (!name.equals(that.name)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    public Optional<Expression> getDefaultValue() {
        return defaultValue;
    }

    public boolean hasDefaultValue() {
        return defaultValue.isPresent();
    }

    @Override

    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + defaultValue.hashCode();
        return result;
    }

    private static class DefaultValuePlaceholder extends Expression {

        @Override
        public Iterable<Node> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public TypeUsage calcType(SymbolResolver resolver) {
            throw new UnsupportedOperationException();
        }
    }

    public static FormalParameter createWithDefaultValuePlaceholder(TypeUsage type, String name) {
        return new FormalParameter(type, name, Optional.of(new DefaultValuePlaceholder()));
    }

    public FormalParameter(TypeUsage type, String name) {
        this(type, name, Optional.empty());
    }

    public FormalParameter(TypeUsage type, String name, Optional<Expression> defaultValue) {
        this.type = type;
        this.type.parent = this;
        this.name = name;
        this.defaultValue = defaultValue;
        if (defaultValue.isPresent()) {
            defaultValue.get().setParent(this);
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        children.add(type);
        if (defaultValue.isPresent()) {
            children.add(defaultValue.get());
        }
        return children;
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        return type;
    }

    public TypeUsage getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
