package me.tomassetti.turin.definitions;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InternalMethodDefinition extends InternalInvokableDefinition {

    private String methodName;
    private JvmMethodDefinition jvmMethodDefinition;
    private TypeUsage returnType;

    public InternalMethodDefinition(String methodName, List<? extends FormalParameter> formalParameters, TypeUsage returnType, JvmMethodDefinition jvmMethodDefinition) {
        super(formalParameters);
        this.methodName = methodName;
        this.returnType = returnType;
        this.jvmMethodDefinition = jvmMethodDefinition;
    }

    public String getMethodName() {
        return methodName;
    }

    public JvmMethodDefinition getJvmMethodDefinition() {
        return jvmMethodDefinition;
    }

    @Override
    public TypeUsage getReturnType() {
        return returnType;
    }

    @Override
    public InternalMethodDefinition apply(Map<String, TypeUsage> typeParams) {
        List<FormalParameter> formalParametersReplaced = new ArrayList<>();
        for (FormalParameter fp : getFormalParameters()) {
            formalParametersReplaced.add(fp.apply(typeParams));
        }
        return new InternalMethodDefinition(methodName,
                formalParametersReplaced, returnType.replaceTypeVariables(typeParams), jvmMethodDefinition);
    }

    @Override
    public InternalConstructorDefinition asConstructor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InternalMethodDefinition asMethod() {
        return this;
    }

    @Override
    public boolean isConstructor() {
        return false;
    }

    @Override
    public boolean isMethod() {
        return true;
    }
}
