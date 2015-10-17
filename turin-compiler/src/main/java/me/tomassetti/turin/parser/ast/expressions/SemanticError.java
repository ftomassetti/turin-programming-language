package me.tomassetti.turin.parser.ast.expressions;

import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.Position;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.Collections;

public class SemanticError extends Expression {

    private String message;
    private Position position;

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
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
    public TypeUsageNode calcType() {
        throw new UnsupportedOperationException();
    }
}
