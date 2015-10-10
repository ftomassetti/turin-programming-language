package me.tomassetti.java.symbol_solver.type_usage;

import me.tomassetti.java.symbol_solver.JavaTypeResolver;
import me.tomassetti.jvm.JvmType;

public class ArrayTypeUsage extends JavaTypeUsage {

    private JavaTypeUsage componentType;

    public ArrayTypeUsage(JavaTypeUsage componentType) {
        this.componentType = componentType;
    }

    public JavaTypeUsage getComponentType() {
        return componentType;
    }

    @Override

    public JvmType jvmType(JavaTypeResolver resolver) {
        return new JvmType("[" + componentType.jvmType(resolver).getSignature());
    }

    @Override
    public ArrayTypeUsage asArrayTypeUsage() {
        return this;
    }

    @Override
    public String toString() {
        return "ArrayTypeUsage{" +
                "componentType=" + componentType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayTypeUsage that = (ArrayTypeUsage) o;

        if (!componentType.equals(that.componentType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return componentType.hashCode();
    }

}
