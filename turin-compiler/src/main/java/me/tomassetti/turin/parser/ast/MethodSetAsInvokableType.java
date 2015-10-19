package me.tomassetti.turin.parser.ast;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.InternalMethodDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.typesystem.InvokableType;
import me.tomassetti.turin.typesystem.MethodResolutionLogic;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.*;

public class MethodSetAsInvokableType implements InvokableType {
    private Set<InternalMethodDefinition> methodDefinitions;
    private Map<String, TypeUsage> typeParams;

    public MethodSetAsInvokableType(Set<InternalMethodDefinition> methodDefinitions, Map<String, TypeUsage> typeParams) {
        this.methodDefinitions = methodDefinitions;
        this.typeParams = typeParams;
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
        Optional<InternalMethodDefinition> method = MethodResolutionLogic.findMethodAmongActualParams(actualParams, new ArrayList<>(methodDefinitions));
        return method.get().getReturnType().replaceTypeVariables(typeParams);
    }

    @Override
    public boolean isOverloaded() {
        return methodDefinitions.size() > 1;
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<ActualParam> actualParams) {
        Optional<InternalMethodDefinition> method = MethodResolutionLogic.findMethodAmongActualParams(actualParams, new ArrayList<>(methodDefinitions));
        return method.get().getJvmMethodDefinition();
    }
}
