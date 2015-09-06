package me.tomassetti.turin.parser.ast;

import java.util.Collections;

/**
 * Represent the absolute context, so it knows only about canonical names.
 */
public class NoContext extends Node {

    private static NoContext INSTANCE = new NoContext();

    public static NoContext getInstance() {
        return INSTANCE;
    }

    private NoContext() {

    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

}
