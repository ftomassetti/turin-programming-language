package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;

public class FunctionReferenceTypeUsageNode extends TypeUsageNode {

    private List<TypeUsageNode> parameterTypes;
    private TypeUsageNode returnType;

    @Override
    public String toString() {
        return "FunctionReferenceTypeUsage{" +
                "parameterTypes=" + parameterTypes +
                ", returnType=" + returnType +
                '}';
    }

    @Override
    public TypeUsageNode copy() {
        throw new UnsupportedOperationException();
    }

    public List<TypeUsageNode> getParameterTypes() {
        return parameterTypes;
    }

    public TypeUsageNode getReturnType() {
        return returnType;
    }

    @Override
    public TypeUsageNode returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver) {
        return returnType;
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
    }

    public FunctionReferenceTypeUsageNode(List<TypeUsageNode> parameterTypes, TypeUsageNode returnType) {
        this.parameterTypes = parameterTypes;
        this.parameterTypes.forEach((pt)->pt.setParent(FunctionReferenceTypeUsageNode.this));
        this.returnType = returnType;
        this.returnType.setParent(this);
    }

    @Override
    public boolean sameType(TypeUsage other, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
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
