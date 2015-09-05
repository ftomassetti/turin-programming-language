package me.tomassetti.turin.parser.ast.imports;

import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.Optional;

public class AllFieldsImportDeclaration extends ImportDeclaration {

    @Override
    public Optional<Node> findAmongImported(String name, Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }
}
