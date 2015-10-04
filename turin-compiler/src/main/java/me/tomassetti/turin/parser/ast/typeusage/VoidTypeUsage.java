package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.Collections;

public class VoidTypeUsage extends TypeUsage {
    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        return new JvmType("V");
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
