package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.ast.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectionTypeDefinitionFactory {

    private static final ReflectionTypeDefinitionFactory INSTANCE = new ReflectionTypeDefinitionFactory();

    public static ReflectionTypeDefinitionFactory getInstance() {
        return INSTANCE;
    }

    public static JvmMethodDefinition toMethodDefinition(Method method){
        return new JvmMethodDefinition(method.getDeclaringClass().getCanonicalName(), method.getName(), calcSignature(method), Modifier.isStatic(method.getModifiers()));
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
        } else if (Void.class.equals(clazz)) {
            return "V";
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

    public TurinTypeDefinition getTypeDefinition(String name) {
        if (name.equals("java.lang.String") || name.equals("java.lang.System") || name.equals("java.io.PrintStream")) {
            return new ReflectionBasedTypeDefinition(name);
        }
        throw new UnsupportedOperationException();
    }

}
