package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;

public class TurinSourceTypeDefinition extends TypeDefinition {

    private TurinTypeDefinition turinTypeDefinition;

    public TurinSourceTypeDefinition(TurinTypeDefinition turinTypeDefinition) {
        super(turinTypeDefinition.getQualifiedName());
        this.turinTypeDefinition = turinTypeDefinition;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public TypeDefinition getSuperclass(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }
}
