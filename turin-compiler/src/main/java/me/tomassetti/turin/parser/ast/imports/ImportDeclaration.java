package me.tomassetti.turin.parser.ast.imports;

import me.tomassetti.turin.parser.ast.Node;

public abstract class ImportDeclaration extends Node {
    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }
}
