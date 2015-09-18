package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.TypeIdentifier;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Optional;

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

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        return new ReferenceTypeUsage(exceptionType.resolve(resolver));
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

    @Override
    public Optional<Node> findSymbol(String name, SymbolResolver resolver) {
        if (name.equals(variableName)) {
            return Optional.of(this);
        }
        return super.findSymbol(name, resolver);
    }
}
