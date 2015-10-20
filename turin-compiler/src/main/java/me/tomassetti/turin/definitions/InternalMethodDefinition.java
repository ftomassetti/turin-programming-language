package me.tomassetti.turin.definitions;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;

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

    public TypeUsage getReturnType() {
        return returnType;
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
