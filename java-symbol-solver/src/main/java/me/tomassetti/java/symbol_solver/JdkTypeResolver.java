package me.tomassetti.java.symbol_solver;

import me.tomassetti.jvm.JvmNameUtils;

import java.util.Optional;

public class JdkTypeResolver implements JavaTypeResolver {

    private static JdkTypeResolver INSTANCE = new JdkTypeResolver();

    private JdkTypeResolver() {

    }

    public static JdkTypeResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<JavaTypeDefinition> resolveAbsoluteTypeName(String typeName) {
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }
        return ReflectionTypeDefinitionFactory.getInstance().findTypeDefinition(typeName);
    }

    @Override
    public boolean existPackage(String packageName) {
        return Package.getPackage(packageName) != null;
    }
}
