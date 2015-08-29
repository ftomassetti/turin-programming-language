package me.tomassetti.turin.ast;

/**
 * Created by federico on 28/08/15.
 */
public class PropertyReference extends Node {

    private String name;

    public PropertyReference(String name) {
        this.name = name;
    }
}
