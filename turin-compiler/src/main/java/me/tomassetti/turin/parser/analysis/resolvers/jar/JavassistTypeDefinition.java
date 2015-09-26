package me.tomassetti.turin.parser.analysis.resolvers.jar;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import me.tomassetti.turin.compiler.SemanticErrorException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.*;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class JavassistTypeDefinition extends TypeDefinition {

    private CtClass ctClass;

    public JavassistTypeDefinition(CtClass ctClass) {
        super(ctClass.getSimpleName());
        if (ctClass.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        if (ctClass.isArray()) {
            throw new IllegalArgumentException();
        }
        this.ctClass = ctClass;
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType(resolver).jvmType(resolver));
            }
        }
        CtMethod method = JavassistBasedMethodResolution.findMethodAmong(methodName, argsTypes, resolver, staticContext, Arrays.asList(ctClass.getMethods()), this);
        try {
            return JavassistTypeDefinitionFactory.toTypeUsage(method.getReturnType());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getQualifiedName() {
        return ctClass.getName();
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        try {
            return JavassistTypeDefinitionFactory.toMethodDefinition(
                    JavassistBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, Arrays.asList(ctClass.getMethods()), this),
                    ctClass.isInterface());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams) {
        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType(resolver).jvmType(resolver));
            }
        }
        try {
            return JavassistTypeDefinitionFactory.toConstructorDefinition(JavassistBasedMethodResolution.findConstructorAmong(argsTypes, resolver, Arrays.asList(ctClass.getConstructors()), this));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TypeUsage getField(String fieldName, boolean staticContext) {
        for (CtField field : ctClass.getFields()) {
            if (field.getName().equals(fieldName)) {
                if (Modifier.isStatic(field.getModifiers()) == staticContext) {
                    try {
                        return JavassistTypeDefinitionFactory.toTypeUsage(field.getType());
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        List<CtMethod> methods = new LinkedList<>();
        for (CtMethod method : ctClass.getMethods()) {
            if (method.getName().equals(fieldName)) {
                if (Modifier.isStatic(method.getModifiers()) == staticContext) {
                    methods.add(method);
                }
            }
        }
        if (!methods.isEmpty()) {
            return typeFor(methods, this);
        }

        // TODO consider inherited fields and methods
        throw new UnsupportedOperationException(fieldName);
    }

    private static TypeUsage typeFor(List<CtMethod> methods, Node parentToAssign) {
        if (methods.isEmpty()) {
            throw new IllegalArgumentException();
        }
        methods.forEach((m)-> {
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new IllegalArgumentException("Non static method given: " + m);
            }
        });
        if (methods.size() != 1) {
            OverloadedFunctionReferenceTypeUsage overloadedFunctionReferenceTypeUsage = new JarOverloadedFunctionReferenceTypeUsage(methods.stream().map((m)->typeFor(m, null)).collect(Collectors.toList()), methods);
            overloadedFunctionReferenceTypeUsage.setParent(parentToAssign);
            return overloadedFunctionReferenceTypeUsage;
        }
        return typeFor(methods.get(0), parentToAssign);
    }

    private static FunctionReferenceTypeUsage typeFor(CtMethod method, Node parentToAssign) {
        try {
            if (method.getGenericSignature() != null) {
                SignatureAttribute.MethodSignature methodSignature = SignatureAttribute.toMethodSignature(method.getGenericSignature());
                SignatureAttribute.Type[] parameterTypes = methodSignature.getParameterTypes();
                List<TypeUsage> paramTypes = Arrays.stream(parameterTypes).map((pt) -> toTypeUsage(pt)).collect(Collectors.toList());
                FunctionReferenceTypeUsage functionReferenceTypeUsage = new FunctionReferenceTypeUsage(paramTypes, toTypeUsage(methodSignature.getReturnType()));
                if (parentToAssign != null) {
                    functionReferenceTypeUsage.setParent(parentToAssign);
                }
                return functionReferenceTypeUsage;
            } else {
                CtClass[] parameterTypes = method.getParameterTypes();
                List<TypeUsage> paramTypes = Arrays.stream(parameterTypes).map((pt) -> toTypeUsage(pt)).collect(Collectors.toList());
                FunctionReferenceTypeUsage functionReferenceTypeUsage = new FunctionReferenceTypeUsage(paramTypes, toTypeUsage(method.getReturnType()));
                if (parentToAssign != null) {
                    functionReferenceTypeUsage.setParent(parentToAssign);
                }
                return functionReferenceTypeUsage;
            }
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static TypeUsage toTypeUsage(CtClass pt) {
        try {
            if (pt.isArray()) {
                return new ArrayTypeUsage(toTypeUsage(pt.getComponentType()));
            } else if (pt.isPrimitive()) {
                return PrimitiveTypeUsage.getByName(pt.getSimpleName());
            } else {
                return new ReferenceTypeUsage(new JavassistTypeDefinition(pt));
            }
        } catch (NotFoundException e){
            throw new RuntimeException(e);
        }
    }

    private static TypeUsage toTypeUsage(SignatureAttribute.Type type) {
        return new JvmType(type.jvmTypeName()).toTypeUsage();
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver) {
        try {
            if (ctClass.getGenericSignature() != null) {
                SignatureAttribute.ClassSignature classSignature = SignatureAttribute.toClassSignature(ctClass.getGenericSignature());
                List<ReferenceTypeUsage> ancestors = new ArrayList<>();
                if (ctClass.getSuperclass() != null) {
                    ReferenceTypeUsage superTypeDefinition = toReferenceTypeUsage(ctClass.getSuperclass(), classSignature.getSuperClass());
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
                }
                int i = 0;
                for (CtClass interfaze : ctClass.getInterfaces()) {
                    SignatureAttribute.ClassType genericInterfaze = classSignature.getInterfaces()[i];
                    ReferenceTypeUsage superTypeDefinition = toReferenceTypeUsage(interfaze, genericInterfaze);
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
                    i++;
                }
                return ancestors;
            } else {
                List<ReferenceTypeUsage> ancestors = new ArrayList<>();
                if (ctClass.getSuperclass() != null) {
                    ReferenceTypeUsage superTypeDefinition = (ReferenceTypeUsage) toTypeUsage(ctClass.getSuperclass());
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
                }
                int i = 0;
                for (CtClass interfaze : ctClass.getInterfaces()) {
                    ReferenceTypeUsage superTypeDefinition = (ReferenceTypeUsage)toTypeUsage(interfaze);
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
                    i++;
                }
                return ancestors;
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        }
    }

    private ReferenceTypeUsage toReferenceTypeUsage(CtClass clazz, SignatureAttribute.ClassType genericClassType) {
        try {
            SignatureAttribute.ClassSignature classSignature = SignatureAttribute.toClassSignature(clazz.getGenericSignature());
            TypeDefinition typeDefinition = new JavassistTypeDefinition(clazz);
            ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(typeDefinition);
            int i=0;
            for (SignatureAttribute.TypeArgument typeArgument : genericClassType.getTypeArguments()) {
                referenceTypeUsage.getTypeParameterValues().add(classSignature.getParameters()[i].getName(), toTypeUsage(typeArgument.getType()));
                i++;
            }
            return referenceTypeUsage;
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        }
    }

    @Override
    public boolean isInterface() {
        return ctClass.isInterface();
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
