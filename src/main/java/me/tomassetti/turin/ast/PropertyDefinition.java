package me.tomassetti.turin.ast;

public class PropertyDefinition extends Node {

    private String name;
    private TypeUsage type;

    public PropertyDefinition(String name, TypeUsage type) {
        this.name = name;
        this.type = type;
    }
}
