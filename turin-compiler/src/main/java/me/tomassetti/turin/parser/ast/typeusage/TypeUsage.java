package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

/**
 * A TypeUsage is the concrete usage of a type int the code.
 * For example it can be a type definition with generic type parameter specified.
 */
public abstract class TypeUsage extends Node {

    private boolean overloaded;

    public abstract JvmType jvmType(SymbolResolver resolver);

    public boolean isReferenceTypeUsage() {
        return false;
    }

    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

    public ArrayTypeUsage asArrayTypeUsage() {
        throw new UnsupportedOperationException();
    }

    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public boolean canBeAssignedTo(TypeUsage type, SymbolResolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public boolean isArray() {
        return this instanceof ArrayTypeUsage;
    }

    public boolean isPrimitive() {
        return this instanceof PrimitiveTypeUsage;
    }

    public boolean isReference() {
        return this instanceof ReferenceTypeUsage;
    }

    public PrimitiveTypeUsage asPrimitiveTypeUsage() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    /**
     * If this is something invokable and can be invoked with the given arguments which type would be return?
     */
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    /**
     * If this has an invokable name with the given methodName and the given arguments which type would be return?
     */
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public abstract boolean isMethodOverloaded(SymbolResolver resolver, String methodName);

    public boolean isOverloaded() {
        return overloaded;
    }
}
