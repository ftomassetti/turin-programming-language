package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.InternalConstructorDefinition;
import me.tomassetti.turin.definitions.InternalInvokableDefinition;
import me.tomassetti.turin.definitions.InternalMethodDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.FormalParameterSymbol;
import me.tomassetti.turin.symbols.Symbol;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A reference to something that has a first goal to be invoked.
 * This could represent a function for example.
 * It represents something not overloaded.
 */
public class InvokableReferenceTypeUsage implements TypeUsage, InvokableType {

    private InternalInvokableDefinition internalInvokableDefinition;

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        if (typeParams.isEmpty()) {
            return this;
        }
        List<FormalParameterSymbol> replacedParams = internalInvokableDefinition
                .getFormalParameters()
                .stream()
                .map((fp) -> new FormalParameterSymbol(
                        fp.getType().replaceTypeVariables(typeParams),
                        fp.getName(),
                        fp.hasDefaultValue()))
                .collect(Collectors.toList());
        if (internalInvokableDefinition.isMethod()) {
            return new InvokableReferenceTypeUsage(new InternalMethodDefinition(
                    internalInvokableDefinition.asMethod().getMethodName(),
                    replacedParams,
                    internalInvokableDefinition.getReturnType().replaceTypeVariables(typeParams),
                    internalInvokableDefinition.asMethod().getJvmMethodDefinition()));
        } else if (internalInvokableDefinition.isConstructor()) {
            return new InvokableReferenceTypeUsage(new InternalConstructorDefinition(
                    internalInvokableDefinition.getReturnType().replaceTypeVariables(typeParams),
                    replacedParams,
                    internalInvokableDefinition.asConstructor().getJvmConstructorDefinition()));
        } else {
            throw new UnsupportedOperationException();
        }
     }

    public InvokableReferenceTypeUsage(InternalInvokableDefinition internalInvokableDefinition) {
        this.internalInvokableDefinition = internalInvokableDefinition;
    }

    /**
     * As a type it should consider only: if it is overloaded and if given parameters
     * with the same type return the same type.
     */
    @Override
    public boolean sameType(TypeUsage other) {
        if (!other.isInvokable()) {
            return false;
        }
        if (!(other instanceof InvokableReferenceTypeUsage)) {
            return false;
        }
        InvokableReferenceTypeUsage otherInvokable = (InvokableReferenceTypeUsage)other;
        if (this.internalInvokableDefinition.getFormalParameters().size() !=
                otherInvokable.internalInvokableDefinition.getFormalParameters().size()) {
            return false;
        }
        for (int i=0; i<this.internalInvokableDefinition.getFormalParameters().size(); i++) {
            if (!this.internalInvokableDefinition.getFormalParameters().get(i).getType().sameType(
                    otherInvokable.internalInvokableDefinition.getFormalParameters().get(i).getType())) {
                return false;
            }
        }
        return this.internalInvokableDefinition.getReturnType().sameType(
                otherInvokable.internalInvokableDefinition.getReturnType());
    }

    @Override
    public boolean isInvokable() {
        return true;
    }

    @Override
    public InvokableType asInvokable() {
        return this;
    }

    @Override
    public JvmType jvmType() {
        throw new UnsupportedOperationException("It has not a direct correspondent on the JVM");
    }

    @Override
    public boolean hasInstanceField(String fieldName, Symbol instance) {
        return false;
    }

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        throw new IllegalArgumentException(describe() + " has no field named " + fieldName);
    }

    @Override
    public Optional<InvokableType> getMethod(String method, boolean staticContext) {
        return Optional.empty();
    }

    /**
     * It cannot be assigned to anything because it is not possible to store a reference to it.
     */
    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        return false;
    }

    @Override
    public boolean isOverloaded() {
        return false;
    }

    @Override
    public Optional<InternalInvokableDefinition> internalInvokableDefinitionFor(List<ActualParam> actualParams) {
        return Optional.of(internalInvokableDefinition);
    }

    @Override
    public String describe() {
        return  "(" +
                String.join(", " , internalInvokableDefinition.getFormalParameters().stream().map((fp)->fp.getType().describe()).collect(Collectors.toList())) + ") -> " +
                internalInvokableDefinition.getReturnType().describe();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvokableReferenceTypeUsage)) return false;

        InvokableReferenceTypeUsage that = (InvokableReferenceTypeUsage) o;

        if (!internalInvokableDefinition.equals(that.internalInvokableDefinition)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return internalInvokableDefinition.hashCode();
    }

    @Override
    public String toString() {
        return "InvokableReferenceTypeUsage{" +
                "internalInvokableDefinition=" + internalInvokableDefinition +
                '}';
    }
}
