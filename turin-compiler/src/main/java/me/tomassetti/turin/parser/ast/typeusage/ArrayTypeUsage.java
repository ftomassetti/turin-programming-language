package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;

public class ArrayTypeUsage extends TypeUsage {

    private TypeUsage componentType;

    public ArrayTypeUsage(TypeUsage componentType) {
        this.componentType = componentType;
    }

    @Override
    public JvmType jvmType(Resolver resolver) {
        return new JvmType("[" + componentType.jvmType(resolver).getSignature());
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(componentType);
    }
}
