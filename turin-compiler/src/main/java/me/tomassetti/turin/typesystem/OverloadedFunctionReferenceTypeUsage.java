package me.tomassetti.turin.typesystem;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.FunctionReferenceTypeUsageNode;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.List;

public abstract class OverloadedFunctionReferenceTypeUsage implements TypeUsage {

    protected List<FunctionReferenceTypeUsage> alternatives;

    OverloadedFunctionReferenceTypeUsage(List<FunctionReferenceTypeUsage> alternatives) {
        if (alternatives.size() < 2) {
            throw new IllegalArgumentException();
        }
        this.alternatives = alternatives;
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

}
