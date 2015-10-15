package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Collections;

/**
 * Node generated when analyzing the AST, node never parsed
 */
public class ResolvedTypeUsage extends TypeUsageNode {

    private TypeDefinition typeDefinition;

    public ResolvedTypeUsage(TypeDefinition typeDefinition) {
        this.typeDefinition = typeDefinition;
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        return typeDefinition.jvmType();
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return typeDefinition.isMethodOverloaded(methodName, resolver);
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public TypeUsageNode copy() {
        return this;
    }
}
