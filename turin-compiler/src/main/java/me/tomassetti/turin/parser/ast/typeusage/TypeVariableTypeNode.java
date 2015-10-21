package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.ConcreteTypeVariableUsage;
import me.tomassetti.turin.typesystem.TypeVariableUsage;

import java.util.List;

public class TypeVariableTypeNode extends TypeUsageWrapperNode {

    @Override
    public TypeUsageNode copy() {
        throw new UnsupportedOperationException();
    }

    private String name;
    private List<TypeUsageNode> bounds;
    private TypeVariableUsage.GenericDeclaration genericDeclaration;

    public TypeVariableTypeNode(TypeVariableUsage.GenericDeclaration genericDeclaration, String name, List<TypeUsageNode> bounds) {
        super(new ConcreteTypeVariableUsage(genericDeclaration, name, bounds));
        this.name = name;
        this.genericDeclaration = genericDeclaration;
        this.bounds = bounds;
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }
}
