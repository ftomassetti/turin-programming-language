package me.tomassetti.turin.parser.ast.expressions.relations;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.relations.RelationDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationFieldDefinition;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

// This is a Symbol, not a Node
public class AccessEndpoint extends Expression {

    private Symbol instance;
    private RelationFieldDefinition relationField;

    public Symbol getInstance() {
        return instance;
    }

    public RelationFieldDefinition getRelationField() {
        return relationField;
    }

    public AccessEndpoint(Symbol instance, RelationFieldDefinition relationField) {
        this.instance = instance;
        this.relationField = relationField;
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsage calcType() {
        return relationField.calcType();
    }

    public RelationDefinition getRelationDefinition() {
        return relationField.getRelationDefinition();
    }
}
