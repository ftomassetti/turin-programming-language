package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.TypeUsage;
import me.tomassetti.turin.typesystem.TypeVariableTypeUsage;

import java.util.List;

public class TypeVariableTypeUsageNode extends TypeUsageWrapperNode {

    @Override
    public TypeUsageNode copy() {
        throw new UnsupportedOperationException();
    }

    private String name;
    private List<TypeUsageNode> bounds;
    private TypeVariableTypeUsage.GenericDeclaration genericDeclaration;

    public TypeVariableTypeUsageNode(TypeVariableTypeUsage.GenericDeclaration genericDeclaration, String name, List<TypeUsageNode> bounds) {
        super(new TypeVariableTypeUsage(genericDeclaration, name, bounds));
        this.name = name;
        this.genericDeclaration = genericDeclaration;
        this.bounds = bounds;
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }
}
