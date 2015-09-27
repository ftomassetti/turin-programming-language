package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

/**
 * Definition of a reference type (a Class, an Interface or an Enum) OR one of the basic types of Turin (like UInt).
 */
public abstract class TypeDefinition extends Node implements Named {
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

    public abstract JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext);

    public abstract JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams);

    public abstract TypeUsage getField(String fieldName, boolean staticContext);

    public abstract List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver);

    public abstract boolean isInterface();

    public Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public abstract boolean hasManyConstructors();

    public abstract boolean isMethodOverloaded(String methodName, SymbolResolver resolver);

    public abstract List<FormalParameter> getConstructorParams(List<ActualParam> actualParams, SymbolResolver resolver);

    public abstract List<FormalParameter> getMethodParams(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    public abstract boolean hasMethodFor(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);
}
