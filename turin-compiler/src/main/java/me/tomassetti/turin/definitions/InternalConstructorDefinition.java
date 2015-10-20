package me.tomassetti.turin.definitions;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InternalConstructorDefinition extends InternalInvokableDefinition {

    private JvmConstructorDefinition jvmConstructorDefinition;
    private TypeUsage returnType;

    public JvmConstructorDefinition getJvmConstructorDefinition() {
        return jvmConstructorDefinition;
    }

    public InternalConstructorDefinition(TypeUsage returnType, List<? extends FormalParameter> formalParameters, JvmConstructorDefinition jvmConstructorDefinition) {
        super(formalParameters);
        this.jvmConstructorDefinition = jvmConstructorDefinition;
        this.returnType = returnType;
    }

    @Override
    public InternalConstructorDefinition asConstructor() {
        return this;
    }

    @Override
    public InternalMethodDefinition asMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConstructor() {
        return true;
    }

    @Override
    public boolean isMethod() {
        return false;
    }

    @Override
    public TypeUsage getReturnType() {
        return returnType;
    }

    @Override
    public InternalConstructorDefinition apply(Map<String, TypeUsage> typeParams) {
        List<FormalParameter> formalParametersReplaced = new ArrayList<>();
        for (FormalParameter fp : getFormalParameters()) {
            formalParametersReplaced.add(fp.apply(typeParams));
        }
        return new InternalConstructorDefinition(returnType.replaceTypeVariables(typeParams),
                formalParametersReplaced, jvmConstructorDefinition);
    }
}
