package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.jvm.JvmTypeCategory;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Map;

abstract class TypeUsageWrapperNode extends TypeUsageNode {

    private TypeUsage typeUsage;

    public final TypeUsage typeUsage() {
        return typeUsage;
    }

    public TypeUsageWrapperNode(TypeUsage typeUsage) {
        this.typeUsage = typeUsage;
    }

    @Override
    public final JvmTypeCategory toJvmTypeCategory(SymbolResolver resolver) {
        return typeUsage().toJvmTypeCategory(resolver);
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
    public final JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        return typeUsage().findMethodFor(name, argsTypes, resolver, staticContext);
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
    public final TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver) {
        return typeUsage().returnTypeWhenInvokedWith(actualParams, resolver);
    }

    @Override
    public final TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return typeUsage().returnTypeWhenInvokedWith(methodName, actualParams, resolver, staticContext);
    }

    @Override
    public final boolean isOverloaded() {
        return typeUsage().isOverloaded();
    }

    @Override
    public final boolean isVoid() {
        return typeUsage().isVoid();
    }

    @Override
    public final JvmType jvmType(SymbolResolver resolver) {
        return typeUsage().jvmType(resolver);
    }

    @Override
    public final ArrayTypeUsage asArrayTypeUsage() {
        return this.typeUsage().asArrayTypeUsage();
    }

    @Override
    public final boolean canBeAssignedTo(TypeUsage type, SymbolResolver resolver) {
        return typeUsage().canBeAssignedTo(type, resolver);
    }

    @Override
    public final Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        return typeUsage().getFieldOnInstance(fieldName, instance, resolver);
    }

    @Override
    public final boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return typeUsage().isMethodOverloaded(resolver, methodName);
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
    public boolean sameType(TypeUsage other, SymbolResolver resolver) {
        return typeUsage().sameType(other, resolver);
    }

}
