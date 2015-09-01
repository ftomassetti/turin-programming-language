package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;

/**
 * Created by federico on 01/09/15.
 */
public class QualifiedName extends Node {

    private QualifiedName base;
    private String name;

    public QualifiedName(QualifiedName base, String name) {

    }

    public QualifiedName(String name) {

    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(base);
    }
}
