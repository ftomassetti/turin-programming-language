package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;

/**
 * Type definition, independent from the source of the type definition (source files, class file, reflection)
 * or the language in which it is defined (Turin, Java, others)
 */
public abstract class TypeDefinition {

    private String canonicalName;

    public TypeDefinition(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public abstract boolean isInterface();

    public abstract boolean isClass();

    public abstract TypeDefinition getSuperclass(SymbolResolver resolver);
}
