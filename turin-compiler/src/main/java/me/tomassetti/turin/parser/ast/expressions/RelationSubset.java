package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.List;

public class RelationSubset extends Expression {

    private String relationName;
    private String fieldName;
    private List<ActualParam> matchingConditions;

    public RelationSubset(String relationName, String fieldName, List<ActualParam> matchingConditions) {
        this.relationName = relationName;
        this.fieldName = fieldName;
        this.matchingConditions = matchingConditions;
        this.matchingConditions.forEach((mc) -> mc.setParent(RelationSubset.this));
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(matchingConditions);
    }

    @Override
    public TypeUsageNode calcType() {
        throw new UnsupportedOperationException();
    }
}
