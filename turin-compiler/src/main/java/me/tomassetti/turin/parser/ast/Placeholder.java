package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.Collections;

public class Placeholder extends Expression {

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public TypeUsageNode calcType() {
        Node parent = getParent();
        while (parent != null) {
            if (parent instanceof PropertyDefinition) {
                PropertyDefinition propertyDefinition = (PropertyDefinition)parent;
                return propertyDefinition.getType();
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException();
    }


}
