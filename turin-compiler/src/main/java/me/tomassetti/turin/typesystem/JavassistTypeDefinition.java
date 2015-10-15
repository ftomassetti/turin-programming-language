package me.tomassetti.turin.typesystem;

import javassist.CtClass;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;

public class JavassistTypeDefinition extends TypeDefinition {

    private CtClass clazz;

    public JavassistTypeDefinition(CtClass clazz) {
        super(clazz.getName());
        this.clazz = clazz;
    }

    @Override
    public boolean isInterface() {
        return clazz.isInterface();
    }

    @Override
    public boolean isClass() {
        return !clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation() && !clazz.isArray() && !clazz.isPrimitive();
    }

    @Override
    public TypeDefinition getSuperclass(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }
}
