package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.FormalParameterSymbol;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.*;

public class FormalParameterNode extends Node implements Symbol, FormalParameter {

    private TypeUsageNode type;
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
        if (!(o instanceof FormalParameterNode)) return false;

        FormalParameterNode that = (FormalParameterNode) o;

        if (!defaultValue.equals(that.defaultValue)) return false;
        if (!name.equals(that.name)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    public Optional<Expression> getDefaultValue() {
        return defaultValue;
    }

    @Override
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
        public TypeUsageNode calcType() {
            throw new UnsupportedOperationException();
        }
    }

    public static FormalParameterNode createWithDefaultValuePlaceholder(TypeUsageNode type, String name) {
        return new FormalParameterNode(type, name, Optional.of(new DefaultValuePlaceholder()));
    }

    public FormalParameterNode(TypeUsageNode type, String name) {
        this(type, name, Optional.empty());
    }

    public FormalParameterNode(TypeUsageNode type, String name, Optional<Expression> defaultValue) {
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
    public TypeUsageNode calcType() {
        return type;
    }

    @Override
    public TypeUsageNode getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FormalParameter apply(Map<String, TypeUsage> typeParams) {
        return new FormalParameterSymbol(type.replaceTypeVariables(typeParams), name, defaultValue.isPresent());
    }
}
