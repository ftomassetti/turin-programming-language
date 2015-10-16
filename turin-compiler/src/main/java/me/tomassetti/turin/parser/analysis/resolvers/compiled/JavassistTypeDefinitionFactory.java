package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.*;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;
import me.tomassetti.turin.typesystem.VoidTypeUsage;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JavassistTypeDefinitionFactory {

    private static final JavassistTypeDefinitionFactory INSTANCE = new JavassistTypeDefinitionFactory();

    public static JavassistTypeDefinitionFactory getInstance() {
        return INSTANCE;
    }

    public static JvmMethodDefinition toMethodDefinition(CtMethod method) throws NotFoundException {
        return toMethodDefinition(method, method.getDeclaringClass().isInterface());
    }

    public static JvmMethodDefinition toMethodDefinition(CtMethod method, boolean onInterface) throws NotFoundException {
        return new JvmMethodDefinition(JvmNameUtils.canonicalToInternal(method.getDeclaringClass().getName()), method.getName(), calcSignature(method), Modifier.isStatic(method.getModifiers()), onInterface);
    }

    public static String calcSignature(CtClass clazz) {
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
                    throw new UnsupportedOperationException(clazz.getName());
            }
        } else if (clazz.isArray()){
            try {
                return "[" + calcSignature(clazz.getComponentType());
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return "L" + clazz.getName().replaceAll("\\.", "/") + ";";
        }
    }

    public static String calcSignature(CtMethod method) throws NotFoundException {
        List<String> paramTypesSignatures = Arrays.stream(method.getParameterTypes()).map((t) -> calcSignature(t)).collect(Collectors.toList());
        return "(" + String.join("", paramTypesSignatures) + ")" + calcSignature(method.getReturnType());
    }

    public static TypeUsage toTypeUsage(CtClass type) {
        if (type.isArray()) {
            try {
                return new ArrayTypeUsage(toTypeUsage(type.getComponentType()));
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        } else if (type.getName().equals(void.class.getCanonicalName())) {
            return new VoidTypeUsage();
        } else if (type.isPrimitive()) {
            return PrimitiveTypeUsageNode.getByName(type.getName());
        } else {
            return new ReferenceTypeUsage(getInstance().getTypeDefinition(type));
        }
    }

    public TypeDefinition getTypeDefinition(CtClass clazz) {
        if (clazz.isArray()) {
            throw new IllegalArgumentException();
        }
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        return new JavassistTypeDefinition(clazz);
    }

    public static JvmConstructorDefinition toConstructorDefinition(CtConstructor constructor) throws NotFoundException {
        return new JvmConstructorDefinition(JvmNameUtils.canonicalToInternal(constructor.getDeclaringClass().getName()), calcSignature(constructor));
    }

    private static String calcSignature(CtConstructor constructor) throws NotFoundException {
        List<String> paramTypesSignatures = Arrays.stream(constructor.getParameterTypes()).map((t) -> calcSignature(t)).collect(Collectors.toList());
        return "(" + String.join("", paramTypesSignatures) + ")V";
    }
}
