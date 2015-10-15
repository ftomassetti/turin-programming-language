package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

abstract class TypeUsageWrapperNode extends TypeUsageNode {

    public abstract TypeUsage typeUsage();

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

}
