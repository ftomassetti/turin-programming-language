package me.tomassetti.turin.parser.ast.imports;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Optional;

public abstract class ImportDeclaration extends Node {

    public abstract Optional<Symbol> findAmongImported(String name, SymbolResolver resolver);
}
