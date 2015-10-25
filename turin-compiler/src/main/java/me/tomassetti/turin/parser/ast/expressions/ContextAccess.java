package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.definitions.ContextDefinition;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Collections;
import java.util.Optional;

public class ContextAccess extends Expression {

    private String contextName;
    private Optional<ContextDefinition> contextSymbol;

    public ContextAccess(String contextName) {
        this.contextName = contextName;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    public Optional<ContextDefinition> contextSymbol() {
        if (contextSymbol == null) {
            contextSymbol = symbolResolver().findContextSymbol(contextName, this);
        }
        return contextSymbol;
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        Optional<ContextDefinition> contextSymbolOptional = resolver.findContextSymbol(contextName, this);
        if (!contextSymbolOptional.isPresent()) {
            errorCollector.recordSemanticError(getPosition(), "Context "+ contextName + " cannot be resolved");
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    @Override
    public TypeUsage calcType() {
        return new ReferenceTypeUsage(
                ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Optional.class, symbolResolver()),
                ImmutableList.of(contextSymbol().get().getType()));
    }
}
