package me.tomassetti.turin.symbols;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.typesystem.TypeUsage;

public class FormalParameterSymbol implements FormalParameter {

    private String name;
    private TypeUsage typeUsage;
    private boolean defaultValue;

    public FormalParameterSymbol(TypeUsage typeUsage, String name) {
        this(typeUsage, name, false);
    }

    public FormalParameterSymbol(TypeUsage typeUsage, String name, boolean defaultValue) {
        this.name = name;
        this.typeUsage = typeUsage;
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean hasDefaultValue() {
        return defaultValue;
    }

    @Override
    public TypeUsage getType() {
        return typeUsage;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        return typeUsage;
    }
}
