package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.FunctionDefinition;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;
import java.util.Optional;

public class ValueReference extends Expression {

    private String name;

    public ValueReference(String name) {
        this.name = name;
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        Optional<Node> declaration = resolver.findSymbol(name, this);
        if (declaration.isPresent()) {
            if (declaration.get() instanceof Expression) {
                return ((Expression) declaration.get()).findMethodFor(argsTypes, resolver, staticContext);
            } else if (declaration.get() instanceof FunctionDefinition) {
                FunctionDefinition functionDefinition = (FunctionDefinition)declaration.get();
                if (functionDefinition.match(argsTypes, resolver)) {
                    return functionDefinition.jvmMethodDefinition(resolver);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new UnsupportedOperationException(declaration.get().getClass().getCanonicalName());
            }
        } else {
            throw new UnsolvedSymbolException(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueReference that = (ValueReference) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    public String getName() {
        return name;
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
    public Node getField(String fieldName, Resolver resolver) {
        return resolve(resolver).getField(fieldName, resolver);
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        Optional<Node> declaration = resolver.findSymbol(name, this);
        if (declaration.isPresent()) {
            return declaration.get().calcType(resolver);
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
