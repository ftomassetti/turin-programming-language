package me.tomassetti.java.symbol_solver;

import me.tomassetti.java.symbol_solver.type_usage.JavaTypeUsage;
import me.tomassetti.jvm.JvmFieldDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionBasedField {

    public JavaTypeUsage calcType(JavaTypeResolver resolver) {
        return ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReflectionBasedField that = (ReflectionBasedField) o;

        if (!field.equals(that.field)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ReflectionBaseField{" +
                "field=" + field +
                '}';
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    private Field field;

    public ReflectionBasedField(Field field) {
        this.field = field;
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    public JvmFieldDefinition toJvmField(JavaTypeResolver resolver) {
        JavaTypeUsage fieldType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
        JavaTypeUsage ownerType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getDeclaringClass());
        return new JvmFieldDefinition(ownerType.jvmType(resolver).getInternalName(), field.getName(), fieldType.jvmType(resolver).getSignature(), true);
    }
}
