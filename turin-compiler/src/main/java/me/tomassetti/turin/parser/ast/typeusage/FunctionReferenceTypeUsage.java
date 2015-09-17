package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

public class FunctionReferenceTypeUsage extends TypeUsage {

    private List<TypeUsage> parameterTypes;
    private TypeUsage returnType;

    @Override
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
        return returnType;
    }

    public FunctionReferenceTypeUsage(List<TypeUsage> parameterTypes, TypeUsage returnType) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    @Override
    public JvmType jvmType(Resolver resolver) {
        return returnType.jvmType(resolver);
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }

}
