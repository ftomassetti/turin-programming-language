package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.Collections;

public class VoidTypeUsage extends TypeUsage {

    @Override
    public JvmType jvmType(Resolver resolver) {
        return new JvmType("V");
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
