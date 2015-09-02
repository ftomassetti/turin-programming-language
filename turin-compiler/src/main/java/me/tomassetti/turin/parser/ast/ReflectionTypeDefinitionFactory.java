package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;

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

    private static class ReflectionBasedTypeDefinition extends TurinTypeDefinition {

        private Class<?> clazz;

        private ReflectionBasedTypeDefinition(String name) {
            super(name);
            try {
                this.clazz = this.getClass().getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e){
                throw new RuntimeException(e);
            }
        }

        private ReflectionBasedTypeDefinition(Class<?> clazz) {
            super(clazz.getCanonicalName());
            this.clazz = clazz;
        }

        @Override
        public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
            List<Method> suitableMethods = new ArrayList<>();
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name)) {
                    if (method.getParameterCount() == argsTypes.size()) {
                        if (Modifier.isStatic(method.getModifiers()) == staticContext) {
                            boolean match = true;
                            for (int i=0; i<argsTypes.size(); i++) {
                                TypeUsage actualType = argsTypes.get(i).toTypeUsage();
                                TypeUsage formalType = toTypeUsage(method.getParameterTypes()[i]);
                                if (!actualType.canBeAssignedTo(formalType, resolver)) {
                                    match = false;
                                }
                            }
                            if (match) {
                                suitableMethods.add(method);
                            }
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

        @Override
        public List<TurinTypeDefinition> getAllAncestors(Resolver resolver) {
            List<TurinTypeDefinition> ancestors = new ArrayList<>();
            if (clazz.getSuperclass() != null) {
                TurinTypeDefinition superTypeDefinition = new ReflectionBasedTypeDefinition(clazz.getSuperclass());
                ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
            }
            for (Class<?> interfaze : clazz.getInterfaces()) {
                TurinTypeDefinition superTypeDefinition = new ReflectionBasedTypeDefinition(interfaze);
                ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
            }
            return ancestors;
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
            return "L" + clazz.getCanonicalName() + ";";
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
