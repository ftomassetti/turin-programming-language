package me.tomassetti.turin.parser.ast.imports;

import me.tomassetti.turin.parser.ast.Node;

import java.util.Optional;

public abstract class ImportDeclaration extends Node {
    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }

    public abstract Optional<Node> findAmongImported(String name);
}
