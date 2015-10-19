package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Map;

abstract class TypeUsageWrapperNode extends TypeUsageNode {

    protected TypeUsage typeUsage;

    @Override
    public String toString() {
        return "TypeUsageWrapperNode{" +
                "typeUsage=" + typeUsage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeUsageWrapperNode)) return false;

        TypeUsageWrapperNode that = (TypeUsageWrapperNode) o;

        if (typeUsage != null ? !typeUsage.equals(that.typeUsage) : that.typeUsage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return typeUsage != null ? typeUsage.hashCode() : 0;
    }

    public TypeUsage typeUsage() {
        if (typeUsage == null) {
            throw new IllegalStateException();
        }
        return typeUsage;
    }

    public TypeUsageWrapperNode(TypeUsage typeUsage) {
        this.typeUsage = typeUsage;
    }

    public TypeUsageWrapperNode() {
        this.typeUsage = null;
    }

    @Override
    public final boolean isReferenceTypeUsage() {
        return typeUsage().isReferenceTypeUsage();
    }

    @Override
    public final ReferenceTypeUsage asReferenceTypeUsage() {
        return typeUsage().asReferenceTypeUsage();
    }

    @Override
    public final JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, boolean staticContext) {
        return typeUsage().findMethodFor(name, argsTypes, staticContext);
    }

    @Override
    public final boolean isArray() {
        return typeUsage().isArray();
    }

    @Override
    public final boolean isPrimitive() {
        return typeUsage().isPrimitive();
    }

    @Override
    public final boolean isReference() {
        return typeUsage().isReference();
    }

    @Override
    public final TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        return typeUsage().returnTypeWhenInvokedWith(methodName, actualParams, staticContext);
    }

    @Override
    public final boolean isVoid() {
        return typeUsage().isVoid();
    }

    @Override
    public final JvmType jvmType() {
        return typeUsage().jvmType();
    }

    @Override
    public final ArrayTypeUsage asArrayTypeUsage() {
        return this.typeUsage().asArrayTypeUsage();
    }

    @Override
    public final boolean canBeAssignedTo(TypeUsage type) {
        return typeUsage().canBeAssignedTo(type);
    }

    @Override
    public final Symbol getInstanceField(String fieldName, Symbol instance) {
        return typeUsage().getInstanceField(fieldName, instance);
    }

    @Override
    public final boolean isMethodOverloaded(String methodName) {
        return typeUsage().isMethodOverloaded(methodName);
    }

    @Override
    public final PrimitiveTypeUsage asPrimitiveTypeUsage() {
        return typeUsage().asPrimitiveTypeUsage();
    }

    @Override
    public final <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        return typeUsage().replaceTypeVariables(typeParams);
    }

    @Override
    public boolean sameType(TypeUsage other) {
        return typeUsage().sameType(other);
    }

}
