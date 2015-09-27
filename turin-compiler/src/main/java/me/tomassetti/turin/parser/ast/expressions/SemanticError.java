package me.tomassetti.turin.parser.ast.expressions;

import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.Position;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;

public class SemanticError extends Expression {

    private String message;
    private Position position;

    @Override
    public boolean validate(SymbolResolver resolver, ErrorCollector errorCollector) {
        errorCollector.recordSemanticError(position, message);
        return false;
    }

    public SemanticError(String message, Position position) {
        this.message = message;
        this.position = position;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }
}
