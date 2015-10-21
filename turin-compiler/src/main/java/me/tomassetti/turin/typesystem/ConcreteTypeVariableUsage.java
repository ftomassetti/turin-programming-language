package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.symbols.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConcreteTypeVariableUsage implements TypeVariableUsage {

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        if (typeParams.containsKey(name)) {
            return typeParams.get(name);
        } else {
            return this;
        }
    }

    private String name;
    private List<TypeUsage> bounds;
    private TypeVariableUsage.GenericDeclaration genericDeclaration;

    public ConcreteTypeVariableUsage(TypeVariableUsage.GenericDeclaration genericDeclaration, String name, List<? extends TypeUsage> bounds) {
        this.name = name;
        this.genericDeclaration = genericDeclaration;
        this.bounds = new ArrayList<>(bounds);
    }

    @Override
    public boolean sameType(TypeUsage other) {
        if (!other.isTypeVariable()) {
            return false;
        }

        TypeVariableUsage otherTypeVariable = other.asTypeVariableUsage();
        return this.getName().equals(otherTypeVariable.getName()) && this.getGenericDeclaration().equals(otherTypeVariable.getGenericDeclaration());
    }

    @Override
    public JvmType jvmType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GenericDeclaration getGenericDeclaration() {
        return genericDeclaration;
    }

    @Override
    public List<TypeUsage> getBounds() {
        return bounds;
    }

    @Override
    public String describe() {
        return "type variable " + name;
    }

    @Override
    public TypeVariableUsage asTypeVariableUsage() {
        return this;
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }
}
