package me.tomassetti.turin.resolvers.jdk;

import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.TypeResolver;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;

import java.util.Optional;

public class JdkTypeResolver implements TypeResolver {

    private static JdkTypeResolver INSTANCE = new JdkTypeResolver();

    private JdkTypeResolver() {

    }

    protected SymbolResolver symbolResolver;

    public SymbolResolver symbolResolver() {
        if (this.root() == this) {
            return symbolResolver.getRoot();
        }
        return this.root().symbolResolver().getRoot();
    }

    @Override
    public void setSymbolResolver(SymbolResolver symbolResolver) {
        this.symbolResolver = symbolResolver;
    }

    private TypeResolver root;

    public TypeResolver root() {
        return root;
    }

    public void setRoot(TypeResolver root) {
        this.root = root;
    }

    public static JdkTypeResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName) {
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }
        return ReflectionTypeDefinitionFactory.getInstance().findTypeDefinition(typeName, symbolResolver());
    }

    @Override
    public Optional<FunctionDefinitionNode> resolveAbsoluteFunctionName(String typeName) {
        return Optional.empty();
    }

    @Override
    public boolean existPackage(String packageName) {
        return Package.getPackage(packageName) != null;
    }
}
