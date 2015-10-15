package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.analysis.symbols_definitions.InternalConstructorDefinition;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;
import java.util.Map;

/**
 * Type definition, independent from the source of the type definition (source files, class file, reflection)
 * or the language in which it is defined (Turin, Java, others)
 */
public abstract class TypeDefinition {

    public TypeDefinition() {
    }

    ///
    /// Naming
    ///

    public abstract String getCanonicalName();

    public final String getName() {
        return JvmNameUtils.canonicalToSimple(getCanonicalName());
    }

    //
    // Typing
    //

    public final JvmType jvmType() {
        return new JvmType(JvmNameUtils.canonicalToDescriptor(getCanonicalName()));
    }

    ///
    /// Classification
    ///

    public abstract boolean isInterface();

    public abstract boolean isClass();

    ///
    /// Hierarchy
    ///

    public abstract TypeDefinition getSuperclass(SymbolResolver resolver);

    public abstract List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver);

    public boolean isMethodOverloaded(String methodName, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public Map<String, TypeUsage> associatedTypeParametersToName(SymbolResolver resolver, List<TypeUsage> typeParams) {
        throw new UnsupportedOperationException();
    }

    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    public <T> T getMethodParams(String name, List<ActualParam> actualParams, SymbolResolver resolver, boolean aStatic) {
        throw new UnsupportedOperationException();
    }

    public Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public JvmMethodDefinition findMethodFor(String methodName, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    public boolean hasField(QualifiedName rest, boolean b, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public boolean hasMethodFor(String getterName, List<Object> objects, SymbolResolver resolver, boolean b) {
        throw new UnsupportedOperationException();
    }

    public List<InternalConstructorDefinition> getConstructors(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }
}
