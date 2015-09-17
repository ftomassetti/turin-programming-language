package me.tomassetti.turin.parser.analysis.resolvers.jdk;

import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.typeusage.ArrayTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReflectionTypeDefinitionFactory {

    private static final ReflectionTypeDefinitionFactory INSTANCE = new ReflectionTypeDefinitionFactory();

    public static ReflectionTypeDefinitionFactory getInstance() {
        return INSTANCE;
    }

    public static JvmMethodDefinition toMethodDefinition(Method method){
        return new JvmMethodDefinition(JvmNameUtils.canonicalToInternal(method.getDeclaringClass().getCanonicalName()), method.getName(), calcSignature(method), Modifier.isStatic(method.getModifiers()));
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
            return new ReferenceTypeUsage(type.getCanonicalName());
        }
    }

    public TypeDefinition getTypeDefinition(Class<?> clazz) {
        if (clazz.isArray()) {
            throw new IllegalArgumentException();
        }
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        return new ReflectionBasedTypeDefinition(clazz);
    }

    public Optional<TypeDefinition> findTypeDefinition(String typeName) {
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
