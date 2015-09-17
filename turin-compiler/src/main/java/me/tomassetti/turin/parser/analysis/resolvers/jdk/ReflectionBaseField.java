package me.tomassetti.turin.parser.analysis.resolvers.jdk;

import me.tomassetti.turin.jvm.JvmFieldDefinition;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

public class ReflectionBaseField extends Node {

    @Override
    public Node getField(String fieldName, Resolver resolver) {
        TypeUsage fieldType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
        return fieldType.getFieldOnInstance(fieldName, this, resolver);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReflectionBaseField that = (ReflectionBaseField) o;

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

    public ReflectionBaseField(Field field) {
        this.field = field;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    public JvmFieldDefinition toJvmField(Resolver resolver) {
        TypeUsage fieldType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
        TypeUsage ownerType = ReflectionTypeDefinitionFactory.toTypeUsage(field.getDeclaringClass());
        return new JvmFieldDefinition(ownerType.jvmType(resolver).getInternalName(), field.getName(), fieldType.jvmType(resolver).getSignature(), true);
    }
}
