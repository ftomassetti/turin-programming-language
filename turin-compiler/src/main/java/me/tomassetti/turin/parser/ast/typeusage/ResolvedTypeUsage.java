package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Collections;

/**
 * Node generated when analyzing the AST, node never parsed
 */
public class ResolvedTypeUsage extends TypeUsage {

    private TypeDefinition typeDefinition;

    public ResolvedTypeUsage(TypeDefinition typeDefinition) {
        this.typeDefinition = typeDefinition;
    }

    @Override
    public JvmType jvmType(Resolver resolver) {
        return typeDefinition.jvmType();
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
