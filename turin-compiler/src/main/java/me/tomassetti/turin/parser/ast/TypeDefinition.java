package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

/**
 * Definition of a reference type (a Class, an Interface or an Enum).
 */
public abstract class TypeDefinition extends Node {
    protected String name;

    public TypeDefinition(String name) {
        this.name = name;
    }

    public abstract String getQualifiedName();

    public String getName() {
        return name;
    }

    public JvmType jvmType() {
        return new JvmType("L" + getQualifiedName().replaceAll("\\.", "/") + ";");
    }

    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public abstract String resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams);

    public TypeUsage getField(String fieldName, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public List<TypeDefinition> getAllAncestors(Resolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
