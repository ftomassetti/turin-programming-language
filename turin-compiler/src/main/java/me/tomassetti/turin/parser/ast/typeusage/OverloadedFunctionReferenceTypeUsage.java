package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public abstract class OverloadedFunctionReferenceTypeUsage extends TypeUsageNode {

    protected List<FunctionReferenceTypeUsage> alternatives;

    OverloadedFunctionReferenceTypeUsage(List<FunctionReferenceTypeUsage> alternatives) {
        if (alternatives.size() < 2) {
            throw new IllegalArgumentException();
        }
        this.alternatives = alternatives;
        this.alternatives.forEach((a)->a.setParent(OverloadedFunctionReferenceTypeUsage.this));
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
