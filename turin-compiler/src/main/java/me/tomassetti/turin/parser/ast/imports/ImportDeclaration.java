package me.tomassetti.turin.parser.ast.imports;

import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.Optional;

public abstract class ImportDeclaration extends Node {

    public abstract Optional<Node> findAmongImported(String name, Resolver resolver);
}
