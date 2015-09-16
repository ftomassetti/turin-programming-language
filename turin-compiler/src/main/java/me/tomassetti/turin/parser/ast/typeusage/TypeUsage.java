package me.tomassetti.turin.parser.ast.typeusage;

import jdk.nashorn.internal.codegen.types.Type;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.reflection.ReflectionBaseField;

import java.util.List;

/**
 * A TypeUsage is the concrete usage of a type int the code.
 * For example it can be a type definition with generic type parameter specified.
 */
public abstract class TypeUsage extends Node {

    public abstract JvmType jvmType(Resolver resolver);

    public boolean isReferenceTypeUsage() {
        return false;
    }

    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

    public ArrayTypeUsage asArrayTypeUsage() {
        throw new UnsupportedOperationException();
    }

    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public boolean canBeAssignedTo(TypeUsage type, Resolver resolver) {
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

    public Node getFieldOnInstance(String fieldName, Node instance, Resolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    /**
     * If this can be invoked with the given arguments which type would be return?
     */
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
