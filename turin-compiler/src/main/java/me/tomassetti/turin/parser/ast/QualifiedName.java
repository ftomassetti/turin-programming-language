package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;

/**
 * Created by federico on 01/09/15.
 */
public class QualifiedName extends Node {

    private QualifiedName base;
    private String name;

    public QualifiedName(QualifiedName base, String name) {
        this.base = base;
        this.name = name;
    }

    public QualifiedName(String name) {
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(base);
    }

    public String qualifiedName() {
        if (base == null) {
            return name;
        } else {
            return base + "." + name;
        }
    }

    @Override
    public String toString() {
        return qualifiedName();
    }
}
