package me.tomassetti.turin.resolvers.jdk;

import me.tomassetti.jvm.JvmFieldDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

public class ReflectionBasedField extends Node {

    @Override
    public TypeUsage calcType() {
        return ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
    }

    @Override
    public Symbol getField(String fieldName, SymbolResolver resolver) {
        TypeUsage fieldType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
        return fieldType.getFieldOnInstance(fieldName, this, resolver);
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

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    public JvmFieldDefinition toJvmField(SymbolResolver resolver) {
        TypeUsage fieldType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
        TypeUsage ownerType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getDeclaringClass());
        return new JvmFieldDefinition(ownerType.jvmType().getInternalName(), field.getName(), fieldType.jvmType().getSignature(), true);
    }
}
