package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;

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

    ///
    /// Classification
    ///

    public abstract boolean isInterface();

    public abstract boolean isClass();

    ///
    /// Hierarchy
    ///

    public abstract TypeDefinition getSuperclass(SymbolResolver resolver);
}
