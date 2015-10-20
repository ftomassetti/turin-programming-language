package me.tomassetti.turin.symbols;

import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Map;

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
    public String toString() {
        return "FormalParameterSymbol{" +
                "name='" + name + '\'' +
                ", typeUsage=" + typeUsage +
                ", defaultValue=" + defaultValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormalParameterSymbol)) return false;

        FormalParameterSymbol that = (FormalParameterSymbol) o;

        if (defaultValue != that.defaultValue) return false;
        if (!name.equals(that.name)) return false;
        if (!typeUsage.equals(that.typeUsage)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeUsage.hashCode();
        result = 31 * result + (defaultValue ? 1 : 0);
        return result;
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
    public TypeUsage calcType() {
        return typeUsage;
    }

    @Override
    public FormalParameter apply(Map<String, TypeUsage> typeParams) {
        return new FormalParameterSymbol(typeUsage.replaceTypeVariables(typeParams), name, defaultValue);
    }
}
