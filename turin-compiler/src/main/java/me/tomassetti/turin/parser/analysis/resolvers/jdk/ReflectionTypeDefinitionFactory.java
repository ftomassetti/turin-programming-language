package me.tomassetti.turin.parser.analysis.resolvers.jdk;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReflectionTypeDefinitionFactory {

    private static final ReflectionTypeDefinitionFactory INSTANCE = new ReflectionTypeDefinitionFactory();

    public static ReflectionTypeDefinitionFactory getInstance() {
        return INSTANCE;
    }

    public static JvmMethodDefinition toMethodDefinition(Method method){
        return new JvmMethodDefinition(JvmNameUtils.internalName(method.getDeclaringClass()), method.getName(), calcSignature(method), Modifier.isStatic(method.getModifiers()), method.getDeclaringClass().isInterface());
    }

    public static String calcSignature(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            switch (clazz.getName()) {
                case "boolean":
                    return "Z";
                case "byte":
                    return "B";
                case "char":
                    return "C";
                case "short":
                    return "S";
                case "int":
                    return "I";
                case "long":
                    return "J";
                case "float":
                    return "F";
                case "double":
                    return "D";
                case "void":
                    return "V";
                default:
                    throw new UnsupportedOperationException(clazz.getCanonicalName());
            }
        } else if (clazz.isArray()){
            return "[" + calcSignature(clazz.getComponentType());
        } else {
            return "L" + clazz.getCanonicalName().replaceAll("\\.", "/") + ";";
        }
    }

    public static String calcSignature(Method method) {
        List<String> paramTypesSignatures = Arrays.stream(method.getParameterTypes()).map((t) -> calcSignature(t)).collect(Collectors.toList());
        return "(" + String.join("", paramTypesSignatures) + ")" + calcSignature(method.getReturnType());
    }

    public static TypeUsage toTypeUsage(Class<?> type) {
        if (type.isArray()) {
            return new ArrayTypeUsage(toTypeUsage(type.getComponentType()));
        } else if (type.isPrimitive()) {
            return PrimitiveTypeUsage.getByName(type.getName());
        } else {
            return new ReferenceTypeUsage(getInstance().getTypeDefinition(type));
        }
    }

    public TypeDefinition getTypeDefinition(Class<?> clazz) {
        return getTypeDefinition(clazz, Collections.emptyList());
    }

    public TypeDefinition getTypeDefinition(Class<?> clazz, List<TypeUsage> typeParams) {
        if (clazz.isArray()) {
            throw new IllegalArgumentException();
        }
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        ReflectionBasedTypeDefinition type = new ReflectionBasedTypeDefinition(clazz);
        for (TypeUsage typeUsage : typeParams) {
            type.addTypeParameter(typeUsage);
        }
        return type;
    }

    public Optional<TypeDefinition> findTypeDefinition(String typeName) {
        if (!typeName.startsWith("java.") && !typeName.startsWith("javax.")) {
            return Optional.empty();
        }
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(typeName);
            return Optional.of(getTypeDefinition(clazz));
        } catch (ClassNotFoundException e) {
           return Optional.empty();
        }
    }

    public static JvmConstructorDefinition toConstructorDefinition(Constructor constructor) {
        return new JvmConstructorDefinition(JvmNameUtils.canonicalToInternal(constructor.getDeclaringClass().getCanonicalName()), calcSignature(constructor));
    }

    private static String calcSignature(Constructor constructor) {
        List<String> paramTypesSignatures = Arrays.stream(constructor.getParameterTypes()).map((t) -> calcSignature(t)).collect(Collectors.toList());
        return "(" + String.join("", paramTypesSignatures) + ")V";
    }

}
