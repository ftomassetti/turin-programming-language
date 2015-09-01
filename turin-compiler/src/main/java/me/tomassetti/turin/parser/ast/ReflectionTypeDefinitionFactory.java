package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;

import java.util.List;

/**
 * Created by federico on 01/09/15.
 */
public class ReflectionTypeDefinitionFactory {

    private static final ReflectionTypeDefinitionFactory INSTANCE = new ReflectionTypeDefinitionFactory();

    public static ReflectionTypeDefinitionFactory getInstance() {
        return INSTANCE;
    }

    private class ReflectionBasedTypeDefinition extends TypeDefinition {

        public ReflectionBasedTypeDefinition(String name) {
            super(name);
        }

        @Override
        public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, Resolver resolver) {
            throw new UnsupportedOperationException();
        }
    }

    public TypeDefinition getTypeDefinition(String name) {
        if (name.equals("java.lang.String") || name.equals("java.lang.System")) {
            return new ReflectionBasedTypeDefinition(name);
        }
        throw new UnsupportedOperationException();
    }

}
