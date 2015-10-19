package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.definitions.InternalMethodDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.typesystem.InvokableType;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Set;

public class MethodSetAsInvokableType implements InvokableType {
    private Set<InternalMethodDefinition> methodDefinitions;

    public MethodSetAsInvokableType(Set<InternalMethodDefinition> methodDefinitions) {
        this.methodDefinitions = methodDefinitions;
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOverloaded() {
        return methodDefinitions.size() > 1;
    }
}
