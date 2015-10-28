package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedSymbolException;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValueReference extends Expression {

    private String name;
    private TypeUsageNode precalculatedType;

    public ValueReference(String name) {
        this.name = name;
    }

    public ValueReference(String name, TypeUsageNode precalculatedType) {
        this.name = name;
        this.precalculatedType = precalculatedType; // parent not set on-purpose
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        List<JvmType> argsTypes = actualParams.stream().map((ap)->ap.getValue().calcType().jvmType()).collect(Collectors.toList());
        Optional<Symbol> declaration = resolver.findSymbol(name, this);
        if (declaration.isPresent()) {
            if (declaration.get() instanceof Expression) {
                return ((Expression) declaration.get()).findMethodFor(actualParams, resolver, staticContext);
            } else if (declaration.get() instanceof FunctionDefinitionNode) {
                FunctionDefinitionNode functionDefinition = (FunctionDefinitionNode)declaration.get();
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
    public Optional<List<? extends FormalParameter>> findFormalParametersFor(InvokableExpr invokable) {
        return resolve(symbolResolver()).findFormalParametersFor(invokable);
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
    public Symbol getField(String fieldName) {
        return resolve(symbolResolver()).getField(fieldName);
    }

    @Override
    public TypeUsage calcType() {
        if (precalculatedType != null) {
            return precalculatedType;
        }
        Optional<Symbol> declaration = symbolResolver().findSymbol(name, this);
        if (declaration.isPresent()) {
            return declaration.get().calcType();
        } else {
            throw new UnsolvedSymbolException(this);
        }
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        try {
            resolve(resolver);
        } catch (UnsolvedSymbolException e) {
            errorCollector.recordSemanticError(getPosition(), "Symbol not found: " + e.getMessage());
        }

        return super.specificValidate(resolver, errorCollector);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    private Symbol cache;

    public Symbol resolve(SymbolResolver resolver) {
        if (cache != null) {
            return cache;
        }
        Optional<Symbol> declaration = resolver.findSymbol(name, this);
        if (declaration.isPresent()) {
            if (!(declaration.get() instanceof Symbol)) {
                throw new UnsupportedOperationException();
            }
            cache = (Symbol)declaration.get();
            return cache;
        } else {
            throw new UnsolvedSymbolException(this);
        }
    }
}
