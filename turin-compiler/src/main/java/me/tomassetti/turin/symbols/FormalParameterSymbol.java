package me.tomassetti.turin.symbols;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Optional;

public class FormalParameterSymbol implements FormalParameter {

    private String name;
    private TypeUsage typeUsage;
    private boolean defaultValue;

    public FormalParameterSymbol(String name, TypeUsage typeUsage, boolean defaultValue) {
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
