package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.TypeIdentifier;

public class CatchClause extends Node {

    private TypeIdentifier exceptionType;
    private BlockStatement body;
    private String variableName;

    public String getVariableName() {
        return variableName;
    }

    public BlockStatement getBody() {
        return body;
    }

    public TypeIdentifier getExceptionType() {
        return exceptionType;
    }

    public CatchClause(TypeIdentifier exceptionType, String variableName, BlockStatement body) {
        this.exceptionType = exceptionType;
        this.exceptionType.setParent(this);
        this.variableName = variableName;
        this.body = body;

        this.body.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(exceptionType, body);
    }
}
