package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public abstract class OverloadedFunctionReferenceTypeUsageNode extends TypeUsageNode {

    protected List<FunctionReferenceTypeUsageNode> alternatives;

    OverloadedFunctionReferenceTypeUsageNode(List<FunctionReferenceTypeUsageNode> alternatives) {
        if (alternatives.size() < 2) {
            throw new IllegalArgumentException();
        }
        this.alternatives = alternatives;
        this.alternatives.forEach((a)->a.setParent(OverloadedFunctionReferenceTypeUsageNode.this));
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(alternatives);
    }
}
