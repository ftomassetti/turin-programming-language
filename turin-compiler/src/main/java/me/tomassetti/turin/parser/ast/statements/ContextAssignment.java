package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.definitions.ContextDefinition;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Collections;
import java.util.Optional;

public class ContextAssignment extends Node {

    private String contextName;
    private Expression contextValue;
    private Optional<ContextDefinition> contextSymbol;

    public ContextAssignment(String contextName, Expression contextValue) {
        this.contextName = contextName;
        this.contextValue = contextValue;
        this.contextValue.setParent(this);
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

    public Expression getContextValue() {
        return contextValue;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(contextValue);
    }
}
