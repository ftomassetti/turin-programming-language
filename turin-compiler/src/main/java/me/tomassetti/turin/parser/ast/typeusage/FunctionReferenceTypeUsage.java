package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

public class FunctionReferenceTypeUsage extends TypeUsage {

    private List<TypeUsage> parameterTypes;
    private TypeUsage returnType;

    @Override
    public String toString() {
        return "FunctionReferenceTypeUsage{" +
                "parameterTypes=" + parameterTypes +
                ", returnType=" + returnType +
                '}';
    }

    public List<TypeUsage> getParameterTypes() {
        return parameterTypes;
    }

    public TypeUsage getReturnType() {
        return returnType;
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
        return returnType;
    }

    public FunctionReferenceTypeUsage(List<TypeUsage> parameterTypes, TypeUsage returnType) {
        this.parameterTypes = parameterTypes;
        this.parameterTypes.forEach((pt)->pt.setParent(FunctionReferenceTypeUsage.this));
        this.returnType = returnType;
        this.returnType.setParent(this);
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        return returnType.jvmType(resolver);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().addAll(parameterTypes).add(returnType).build();
    }

}
