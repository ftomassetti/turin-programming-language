package me.tomassetti.turin.parser.ast.expressions.relations;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.relations.RelationDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationFieldDefinition;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

public class AccessEndpoint extends Expression {

    private Node instance;
    private RelationFieldDefinition relationField;

    public Node getInstance() {
        return instance;
    }

    public RelationFieldDefinition getRelationField() {
        return relationField;
    }

    public AccessEndpoint(Node instance, RelationFieldDefinition relationField) {
        this.instance = instance;
        this.relationField = relationField;
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsageNode calcType(SymbolResolver resolver) {
        return relationField.calcType(resolver);
    }

    public RelationDefinition getRelationDefinition() {
        return relationField.getRelationDefinition();
    }
}
