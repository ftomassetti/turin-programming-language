package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsageNode;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

public class NotOperation extends Expression {
    private Expression value;

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(value);
    }

    @Override
    public TypeUsageNode calcType(SymbolResolver resolver) {
        return PrimitiveTypeUsageNode.BOOLEAN;
    }

    public Expression getValue() {
        return value;
    }

    public NotOperation(Expression value) {
        this.value = value;
        this.value.setParent(this);
    }
}
