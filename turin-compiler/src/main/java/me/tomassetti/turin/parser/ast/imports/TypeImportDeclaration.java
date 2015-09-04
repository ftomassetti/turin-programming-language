package me.tomassetti.turin.parser.ast.imports;

import me.tomassetti.turin.parser.ast.Node;

import java.util.Optional;

public class TypeImportDeclaration extends ImportDeclaration {

    @Override
    public Optional<Node> findAmongImported(String name) {
        throw new UnsupportedOperationException();
    }

}
