package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.parser.ast.Node;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public enum ResolverRegistry implements ResolverProvider {

    INSTANCE;

    public void record(Node node, SymbolResolver resolver) {
        if (!node.isRoot()) {
            throw new IllegalArgumentException();
        }
        resolvers.put(node, resolver);
    }

    @Override
    public Optional<SymbolResolver> findResolver(Node node) {
        Node root = node.getRoot();
        if (resolvers.containsKey(root)) {
            return Optional.of(resolvers.get(node));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public SymbolResolver requireResolver(Node node) {
        Optional<SymbolResolver> or = findResolver(node);
        if (or.isPresent()) {
            return or.get();
        } else {
            throw new IllegalStateException();
        }
    }

    private Map<Node, SymbolResolver> resolvers = new IdentityHashMap<>();

}
