package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.analysis.Unresolved;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by federico on 01/09/15.
 */
public class ReflectionTypeDefinitionFactory {

    private static final ReflectionTypeDefinitionFactory INSTANCE = new ReflectionTypeDefinitionFactory();

    public static ReflectionTypeDefinitionFactory getInstance() {
        return INSTANCE;
    }

    private static class ReflectionBasedTypeDefinition extends TypeDefinition {

        private Class<?> clazz;

        public ReflectionBasedTypeDefinition(String name) {
            super(name);
            try {
                this.clazz = this.getClass().getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
            List<Method> suitableMethods = new ArrayList<>();
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name)) {
                    if (method.getParameterCount() == argsTypes.size()) {
                        if (Modifier.isStatic(method.getModifiers()) == staticContext) {
                            suitableMethods.add(method);
                        }
                    }
                }
            }

            if (suitableMethods.size() == 0) {
                throw new RuntimeException("unresolved method " + name);
            } else if (suitableMethods.size() == 1) {
                return toMethodDefinition(suitableMethods.get(0));
            } else {
                throw new UnsupportedOperationException(suitableMethods.toString());
            }
        }

        @Override
        public TypeUsage getField(String fieldName, boolean staticContext) {
            for (Field field : clazz.getFields()) {
                if (field.getName().equals(fieldName)) {
                    if (Modifier.isStatic(field.getModifiers()) == staticContext) {
                        return toTypeUsage(field.getType());
                    }
                }
            }
            throw new UnsupportedOperationException();
        }
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
                default:
                    throw new UnsupportedOperationException(clazz.getCanonicalName());
            }
        } else if (Void.class.equals(clazz)) {
            return "V";
        } else if (clazz.isArray()){
            return "[" + calcSignature(clazz.getComponentType());
        } else {
            return "L" + clazz.getCanonicalName() + ";";
        }
    }

    public static String calcSignature(Method method) {
        List<String> paramTypesSignatures = Arrays.stream(method.getParameterTypes()).map((t) -> calcSignature(t)).collect(Collectors.toList());
        return "(" + String.join("", paramTypesSignatures) + ")" + calcSignature(method.getReturnType());
    }

    public static TypeUsage toTypeUsage(Class<?> type) {
        if (type.isArray()) {
            throw new UnsupportedOperationException();
        } else if (type.isPrimitive()) {
            throw new UnsupportedOperationException();
        } else {
            return new ReferenceTypeUsage(type.getCanonicalName());
        }
    }

    public TypeDefinition getTypeDefinition(String name) {
        if (name.equals("java.lang.String") || name.equals("java.lang.System") || name.equals("java.io.PrintStream")) {
            return new ReflectionBasedTypeDefinition(name);
        }
        throw new UnsupportedOperationException();
    }

}
