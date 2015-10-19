package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.symbols.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypeVariableUsage implements TypeUsage {

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        if (typeParams.containsKey(name)) {
            return typeParams.get(name);
        } else {
            return this;
        }
    }

    public static class GenericDeclaration {

        private String className;
        private String methodSignature;
        private String constructorSignature;

        public boolean isDeclaredOnClass() {
            return methodSignature == null && constructorSignature == null;
        }

        public boolean isDeclaredOnMethod() {
            return methodSignature != null;
        }

        public boolean isDeclaredOnConstructor() {
            return constructorSignature != null;
        }

        private GenericDeclaration(String className, String constructorSignature, String methodSignature) {
            this.className = className;
            this.constructorSignature = constructorSignature;
            this.methodSignature = methodSignature;
        }

        public static GenericDeclaration onClass(String className) {
            return new GenericDeclaration(className, null, null);
        }

        public static GenericDeclaration onMethod(String className, String methodSignature) {
            return new GenericDeclaration(className, methodSignature, null);
        }

        public static GenericDeclaration onConstructor(String className, String constructorSignature) {
            return new GenericDeclaration(className, null, constructorSignature);
        }
    }

    private String name;
    private List<TypeUsage> bounds;
    private GenericDeclaration genericDeclaration;

    public TypeVariableUsage(GenericDeclaration genericDeclaration, String name, List<? extends TypeUsage> bounds) {
        this.name = name;
        this.genericDeclaration = genericDeclaration;
        this.bounds = new ArrayList<>(bounds);
    }

    @Override
    public boolean sameType(TypeUsage other) {
        /*if (!other.isTypeVariable()) {
            return false;
        }

        return this.getName()*/
        throw new UnsupportedOperationException();
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

}
