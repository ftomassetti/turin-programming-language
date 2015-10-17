package me.tomassetti.turin.resolvers;

import me.tomassetti.turin.parser.ast.Node;

import java.util.Optional;

/**
 * Created by federico on 16/10/15.
 */
public interface ResolverProvider {
    Optional<SymbolResolver> findResolver(Node node);

    SymbolResolver requireResolver(Node node);
}
