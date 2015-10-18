package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.Symbol;

import java.util.List;
import java.util.Map;

public class FunctionReferenceTypeUsage implements TypeUsage {

    private List<? extends TypeUsage> parameterTypes;
    private TypeUsage returnType;

    @Override
    public String toString() {
        return "FunctionReferenceTypeUsage{" +
                "parameterTypes=" + parameterTypes +
                ", returnType=" + returnType +
                '}';
    }

    public List<? extends TypeUsage> getParameterTypes() {
        return parameterTypes;
    }

    public TypeUsage getReturnType() {
        return returnType;
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver) {
        return returnType;
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
    }

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        throw new UnsupportedOperationException();
    }

    public FunctionReferenceTypeUsage(List<? extends TypeUsage> parameterTypes, TypeUsage returnType) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    @Override
    public boolean sameType(TypeUsage other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmType jvmType() {
        return returnType.jvmType();
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type, SymbolResolver resolver) {
        return false;
    }

    @Override
    public Symbol getFieldOnInstance(String fieldName, Symbol instance, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

}
