package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Optional;

public class ValueReference extends Expression {

    private String name;

    public ValueReference(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueReference that = (ValueReference) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {

        return "ValueReference{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        Optional<Node> declaration = resolver.findSymbol(name, this);
        if (declaration.isPresent()) {
            if (declaration.get() instanceof Expression) {
                Expression expression = (Expression)declaration.get();
                return expression.calcType(resolver);
            } else {
                throw new UnsupportedOperationException(declaration.get().describe());
            }
        } else {
            throw new UnsolvedSymbolException(this);
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    public Node resolve(Resolver resolver) {
        Optional<Node> declaration = resolver.findSymbol(name, this);
        if (declaration.isPresent()) {
            return declaration.get();
        } else {
            throw new UnsolvedSymbolException(this);
        }
    }
}
