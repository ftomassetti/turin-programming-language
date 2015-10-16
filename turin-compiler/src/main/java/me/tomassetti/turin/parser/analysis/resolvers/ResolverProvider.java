package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.Optional;

/**
 * Created by federico on 16/10/15.
 */
public interface ResolverProvider {
    Optional<SymbolResolver> findResolver(Node node);

    SymbolResolver requireResolver(Node node);
}
