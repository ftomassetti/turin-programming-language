package me.tomassetti.turin.resolvers.jdk;

import me.tomassetti.jvm.JvmFieldDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionBasedField implements Symbol {

    @Override
    public TypeUsage calcType() {
        return ReflectionTypeDefinitionFactory.toTypeUsage(field.getType(), symbolResolver);
    }

    @Override
    public Symbol getField(String fieldName) {
        TypeUsage fieldType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getType(), symbolResolver);
        return fieldType.getInstanceField(fieldName, this);
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
    private SymbolResolver symbolResolver;

    public ReflectionBasedField(Field field, SymbolResolver symbolResolver) {
        this.field = field;
        this.symbolResolver = symbolResolver;
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    public JvmFieldDefinition toJvmField(SymbolResolver resolver) {
        TypeUsage fieldType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getType(), symbolResolver);
        TypeUsage ownerType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getDeclaringClass(), symbolResolver);
        return new JvmFieldDefinition(ownerType.jvmType().getInternalName(), field.getName(), fieldType.jvmType().getSignature(), true);
    }
}
