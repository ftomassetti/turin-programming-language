package me.tomassetti.turin.resolvers.compiled;

import javassist.*;
import javassist.bytecode.*;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.compiler.errorhandling.SemanticErrorException;
import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.InternalConstructorDefinition;
import me.tomassetti.turin.definitions.InternalMethodDefinition;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.*;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.FormalParameterSymbol;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.*;
import org.objectweb.asm.signature.SignatureReader;
import turin.compilation.DefaultParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class JavassistTypeDefinition implements TypeDefinition {

    private CtClass ctClass;
    private SymbolResolver resolver;

    public JavassistTypeDefinition(CtClass ctClass, SymbolResolver resolver) {
        this.resolver = resolver;
        if (ctClass.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        if (ctClass.isArray()) {
            throw new IllegalArgumentException();
        }
        this.ctClass = ctClass;
    }

    @Override
    public boolean hasField(String name, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<InternalConstructorDefinition> getConstructors() {
        return Arrays.stream(ctClass.getConstructors())
                .map((c) -> toInternalConstructorDefinition(c, resolver))
                .collect(Collectors.toList());
    }

    private InternalConstructorDefinition toInternalConstructorDefinition(CtConstructor constructor, SymbolResolver resolver) {
        try {
            JvmConstructorDefinition jvmConstructorDefinition = JavassistTypeDefinitionFactory.toConstructorDefinition(constructor);
            return new InternalConstructorDefinition(formalParameters(constructor, resolver), jvmConstructorDefinition);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isMethodOverloaded(String methodName) {
        return Arrays.stream(ctClass.getMethods()).filter((m)->m.getName().equals(methodName)).count() > 1;
    }

    @Override
    public String getName() {
        return ctClass.getSimpleName();
    }

    @Override
    public TypeUsage calcType() {
        return new ReferenceTypeUsage(this);
    }

    class DefaultParamData {
        String name;
        String signature;
        int index;
    }

    private List<DefaultParamData> getDefaultParamData(CtBehavior behavior) {
        List<DefaultParamData> defaultParams = new LinkedList<>();
        try {
            for (Object annotation : behavior.getAnnotations()) {
                if (Proxy.isProxyClass(annotation.getClass()) && DefaultParam.class.isInstance(annotation)) {
                    DefaultParam defaultParam = (DefaultParam)annotation;
                    DefaultParamData defaultParamData = new DefaultParamData();
                    defaultParamData.name = defaultParam.name();
                    defaultParamData.signature = defaultParam.typeSignature();
                    defaultParamData.index = defaultParam.index();
                    defaultParams.add(defaultParamData);
                } else {
                    String annotationName = annotation.getClass().getCanonicalName();
                    if (annotationName.equals(DefaultParam.class.getCanonicalName())) {
                        try {
                            DefaultParamData defaultParam = new DefaultParamData();
                            defaultParam.name = (String)annotation.getClass().getMethod("name").invoke(annotation);
                            defaultParam.signature = (String)annotation.getClass().getMethod("signature").invoke(annotation);
                            defaultParam.index = (int)annotation.getClass().getMethod("index").invoke(annotation);
                            defaultParams.add(defaultParam);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return defaultParams;
    }

    private boolean hasDefaultParamAnnotation(CtBehavior ctBehavior) {
        return getDefaultParamData(ctBehavior).size() > 0;
    }

    private List<? extends FormalParameter> getFormalParametersConsideringDefaultParams(CtBehavior ctBehavior, SymbolResolver resolver) {
        List<DefaultParamData> defaultParamDatas = getDefaultParamData(ctBehavior);
        defaultParamDatas.sort(new Comparator<DefaultParamData>() {
            @Override
            public int compare(DefaultParamData o1, DefaultParamData o2) {
                return Integer.compare(o1.index, o2.index);
            }
        });
        List<FormalParameterSymbol> formalParameters = new ArrayList<>();
        try {
            MethodInfo methodInfo = ctBehavior.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

            // the last one is the map of default params so we skip it
            for (int i=0;i<ctBehavior.getParameterTypes().length - 1;i++) {
                CtClass type = ctBehavior.getParameterTypes()[i];
                String paramName = attr.variableName(i);
                formalParameters.add(new FormalParameterSymbol(toTypeUsage(type, resolver), paramName));
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        for (DefaultParamData defaultParamData : defaultParamDatas) {
            TypeUsage paramType = TypeUsageNode.fromJvmType(new JvmType(defaultParamData.signature), resolver, Collections.emptyMap());
            formalParameters.add(new FormalParameterSymbol(paramType, defaultParamData.name, true));
        }
        return formalParameters;
    }

    @Override
    public Optional<InternalMethodDefinition> findMethod(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        List<CtMethod> candidates = Arrays.asList(ctClass.getMethods());
        Optional<CtMethod> method = JavassistBasedMethodResolution.findMethodAmongActualParams(methodName,
                actualParams, resolver, staticContext, candidates);
        if (method.isPresent()) {
            return Optional.of(toInternalMethodDefinition(method.get(), resolver));
        } else {
            return Optional.empty();
        }
    }

    private Map<String, TypeUsageNode.TypeVariableData> getVisibleTypeVariables(CtClass ctClass) {
        Map<String, TypeUsageNode.TypeVariableData> visibleTypeVariables = new HashMap<>();
        if (ctClass.getGenericSignature() != null) {
            try {
                SignatureAttribute.ClassSignature classSignature = SignatureAttribute.toClassSignature(ctClass.getGenericSignature());
                for (SignatureAttribute.TypeParameter typeParameter : classSignature.getParameters()) {
                    TypeVariableUsage.GenericDeclaration genericDeclaration = null;
                    List<? extends TypeUsage> bounds = Collections.emptyList();
                    TypeUsageNode.TypeVariableData typeVariableData = new TypeUsageNode.TypeVariableData(genericDeclaration, bounds);
                    visibleTypeVariables.put(typeParameter.getName(), typeVariableData);
                }
            } catch (BadBytecode badBytecode) {
                throw new RuntimeException(badBytecode);
            }
        }
        return visibleTypeVariables;
    }

    private Map<String, TypeUsageNode.TypeVariableData> getVisibleTypeVariables(CtMethod method) {
        CtClass ctClass = method.getDeclaringClass();
        Map<String, TypeUsageNode.TypeVariableData> visibleTypeVariables = getVisibleTypeVariables(ctClass);
        return visibleTypeVariables;
    }

    private InternalMethodDefinition toInternalMethodDefinition(CtMethod ctMethod, SymbolResolver resolver) {
        try {
            Map<String, TypeUsageNode.TypeVariableData> visibleTypeVariables = getVisibleTypeVariables(ctMethod);
            TypeUsage returnType = toTypeUsage(ctMethod.getReturnType(), resolver);
            if (ctMethod.getGenericSignature() != null) {
                SignatureAttribute.MethodSignature methodSignature = SignatureAttribute.toMethodSignature(ctMethod.getGenericSignature());
                returnType = toTypeUsage(methodSignature.getReturnType(), resolver, visibleTypeVariables);
            }
            return new InternalMethodDefinition(ctMethod.getName(), formalParameters(ctMethod, resolver),
                    returnType, JavassistTypeDefinitionFactory.toMethodDefinition(ctMethod));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        }
    }

    private List<? extends FormalParameter> formalParameters(CtConstructor constructor, SymbolResolver resolver) {
        if (hasDefaultParamAnnotation(constructor)) {
            return getFormalParametersConsideringDefaultParams(ctClass.getConstructors()[0], resolver);
        }
        try {
            List<FormalParameterSymbol> formalParameters = new ArrayList<>();
            MethodInfo methodInfo = constructor.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            int i=0;
            for (CtClass type : constructor.getParameterTypes()) {
                formalParameters.add(new FormalParameterSymbol(toTypeUsage(type, resolver), attr.variableName(i)));
                i++;
            }
            return formalParameters;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<FormalParameterSymbol> formalParameters(CtMethod method, SymbolResolver resolver) {
        try {
            List<FormalParameterSymbol> formalParameters = new ArrayList<>();
            MethodInfo methodInfo = method.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            int i=0;
            for (CtClass type : method.getParameterTypes()) {
                formalParameters.add(new FormalParameterSymbol(toTypeUsage(type, resolver),  attr.variableName(i)));
                i++;
            }
            return formalParameters;
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
                    JavassistBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, Arrays.asList(ctClass.getMethods())),
                    ctClass.isInterface());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(List<ActualParam> actualParams) {
        // if this is the compiled version of a turin type we have to handle default parameters
        if (ctClass.getConstructors().length == 1 && hasDefaultParamAnnotation(ctClass.getConstructors()[0])) {
            try {
                return JavassistTypeDefinitionFactory.toConstructorDefinition(ctClass.getConstructors()[0]);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType().jvmType());
            }
        }
        try {
            return JavassistTypeDefinitionFactory.toConstructorDefinition(JavassistBasedMethodResolution.findConstructorAmong(argsTypes, resolver, Arrays.asList(ctClass.getConstructors())));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TypeUsage getFieldType(String fieldName, boolean staticContext) {
        for (CtField field : ctClass.getFields()) {
            if (field.getName().equals(fieldName)) {
                if (Modifier.isStatic(field.getModifiers()) == staticContext) {
                    try {
                        return JavassistTypeDefinitionFactory.toTypeUsage(field.getType(), resolver);
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
            return typeFor(methods, resolver);
        }

        // TODO consider inherited fields and methods
        throw new UnsupportedOperationException(fieldName);
    }

    @Override
    public Symbol getFieldOnInstance(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException();
    }

    private static TypeUsage typeFor(List<CtMethod> methods, SymbolResolver resolver) {
        if (methods.isEmpty()) {
            throw new IllegalArgumentException();
        }
        methods.forEach((m)-> {
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new IllegalArgumentException("Non static method given: " + m);
            }
        });
        if (methods.size() != 1) {
            OverloadedFunctionReferenceTypeUsage overloadedFunctionReferenceTypeUsage = new JarOverloadedFunctionReferenceTypeUsage(
                    methods.stream().map((m)->typeFor(m, resolver)).collect(Collectors.toList()),
                    methods,
                    resolver);
            return overloadedFunctionReferenceTypeUsage;
        }
        return typeFor(methods.get(0), resolver);
    }

    private static FunctionReferenceTypeUsage typeFor(CtMethod method, SymbolResolver resolver) {
        try {
            if (method.getGenericSignature() != null) {
                SignatureAttribute.MethodSignature methodSignature = SignatureAttribute.toMethodSignature(method.getGenericSignature());
                SignatureAttribute.Type[] parameterTypes = methodSignature.getParameterTypes();
                List<TypeUsage> paramTypes = Arrays.stream(parameterTypes).map((pt) -> toTypeUsage(pt, resolver, Collections.emptyMap())).collect(Collectors.toList());
                FunctionReferenceTypeUsage functionReferenceTypeUsage = new FunctionReferenceTypeUsage(paramTypes, toTypeUsage(methodSignature.getReturnType(), resolver, Collections.emptyMap()));
                return functionReferenceTypeUsage;
            } else {
                CtClass[] parameterTypes = method.getParameterTypes();
                List<TypeUsage> paramTypes = Arrays.stream(parameterTypes).map((pt) -> toTypeUsage(pt, resolver)).collect(Collectors.toList());
                FunctionReferenceTypeUsage functionReferenceTypeUsage = new FunctionReferenceTypeUsage(paramTypes, toTypeUsage(method.getReturnType(), resolver));
                return functionReferenceTypeUsage;
            }
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static TypeUsage toTypeUsage(CtClass pt, SymbolResolver symbolResolver) {
        try {
            if (pt.isArray()) {
                return new ArrayTypeUsage(toTypeUsage(pt.getComponentType(), symbolResolver));
            } else if (pt.getName().equals(void.class.getCanonicalName())) {
                return new VoidTypeUsageNode();
            } else if (pt.isPrimitive()) {
                return PrimitiveTypeUsage.getByName(pt.getSimpleName());
            } else {
                return new ReferenceTypeUsage(new JavassistTypeDefinition(pt, symbolResolver));
            }
        } catch (NotFoundException e){
            throw new RuntimeException(e);
        }
    }

    private static JvmType toJvmType(SignatureAttribute.Type type, Map<String, TypeUsageNode.TypeVariableData> visibleGenericTypes) {
        if (type.jvmTypeName().equals("void")) {
            return JvmType.VOID;
        } else if (PrimitiveTypeUsage.isPrimitiveTypeName(type.jvmTypeName())) {
            return PrimitiveTypeUsage.getByName(type.jvmTypeName()).jvmType();
        } else {
            if (visibleGenericTypes.keySet().contains(type.jvmTypeName())) {
                throw new UnsupportedOperationException();
            }

            // remove generic parameters
            String signature = type.jvmTypeName();
            int index = signature.indexOf('<');
            if (index != -1) {
                signature = signature.substring(0, index);
            }
            return new JvmType("L" + JvmNameUtils.canonicalToInternal(signature) + ";");
        }

    }

    private static TypeUsage toTypeUsage(SignatureAttribute.Type type, SymbolResolver resolver, Map<String, TypeUsageNode.TypeVariableData> visibleGenericTypes) {
        if (visibleGenericTypes.keySet().contains(type.jvmTypeName())) {
            TypeUsageNode.TypeVariableData typeVariableData = visibleGenericTypes.get(type.jvmTypeName());
            return new TypeVariableUsage(typeVariableData.getGenericDeclaration(), type.jvmTypeName(), typeVariableData.getBounds());
        }
        return TypeUsageNode.fromJvmType(toJvmType(type, visibleGenericTypes), resolver, visibleGenericTypes);
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors() {
        try {
            if (ctClass.getGenericSignature() != null) {
                SignatureAttribute.ClassSignature classSignature = SignatureAttribute.toClassSignature(ctClass.getGenericSignature());
                List<ReferenceTypeUsage> ancestors = new ArrayList<>();
                if (ctClass.getSuperclass() != null) {
                    ReferenceTypeUsage superTypeDefinition = toReferenceTypeUsage(ctClass.getSuperclass(), classSignature.getSuperClass(), resolver);
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors());
                }
                int i = 0;
                for (CtClass interfaze : ctClass.getInterfaces()) {
                    SignatureAttribute.ClassType genericInterfaze = classSignature.getInterfaces()[i];
                    ReferenceTypeUsage superTypeDefinition = toReferenceTypeUsage(interfaze, genericInterfaze, resolver);
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors());
                    i++;
                }
                return ancestors;
            } else {
                List<ReferenceTypeUsage> ancestors = new ArrayList<>();
                if (ctClass.getSuperclass() != null) {
                    ReferenceTypeUsage superTypeDefinition = toTypeUsage(ctClass.getSuperclass(), resolver).asReferenceTypeUsage();
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors());
                }
                int i = 0;
                for (CtClass interfaze : ctClass.getInterfaces()) {
                    ReferenceTypeUsage superTypeDefinition = toTypeUsage(interfaze, resolver).asReferenceTypeUsage();
                    ancestors.add(superTypeDefinition);
                    ancestors.addAll(superTypeDefinition.getAllAncestors());
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

    private ReferenceTypeUsage toReferenceTypeUsage(CtClass clazz, SignatureAttribute.ClassType genericClassType, SymbolResolver resolver) {
        try {
            SignatureAttribute.ClassSignature classSignature = SignatureAttribute.toClassSignature(clazz.getGenericSignature());
            TypeDefinition typeDefinition = new JavassistTypeDefinition(clazz, this.resolver);
            ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(typeDefinition);
            int i=0;
            for (SignatureAttribute.TypeArgument typeArgument : genericClassType.getTypeArguments()) {
                SignatureAttribute.Type t = typeArgument.getType();
                referenceTypeUsage.getTypeParameterValues().add(classSignature.getParameters()[i].getName(),
                        toTypeUsage(t, resolver, Collections.emptyMap()));
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
    public boolean isClass() {
        return !ctClass.isInterface() && !ctClass.isArray() && !ctClass.isPrimitive() && !ctClass.isAnnotation() && !ctClass.isEnum();
    }

    @Override
    public boolean canFieldBeAssigned(String field, SymbolResolver resolver) {
        return true;
    }

    @Override
    public TypeDefinition getSuperclass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends TypeUsage> Map<String, TypeUsage> associatedTypeParametersToName(SymbolResolver resolver, List<T> typeParams) {
        if (typeParams.isEmpty()) {
            return Collections.emptyMap();
        }
        String genericSignature = ctClass.getGenericSignature();
        try {
            SignatureAttribute.ClassSignature classSignature =SignatureAttribute.toClassSignature(genericSignature);
            SignatureAttribute.TypeParameter[] typeParameters = classSignature.getParameters();
            if (typeParameters.length != typeParams.size()) {
                throw new IllegalStateException("It should have " + typeParameters.length + " and it has " + typeParams.size());
            }
            Map<String, TypeUsage> map = new HashMap<>();
            int i=0;
            for (SignatureAttribute.TypeParameter tv : typeParameters) {
                map.put(tv.getName(), typeParams.get(i));
                i++;
            }
            return map;
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        }
    }

    @Override
    public Optional<InternalConstructorDefinition> findConstructor(List<ActualParam> actualParams) {
        // if this is the compiled version of a turin type we have to handle default parameters
        if (ctClass.getConstructors().length == 1 && hasDefaultParamAnnotation(ctClass.getConstructors()[0])) {
            return Optional.of(toInternalConstructorDefinition(ctClass.getConstructors()[0], resolver));
        }

        CtConstructor constructor = JavassistBasedMethodResolution.findConstructorAmongActualParams(
                actualParams, resolver, Arrays.asList(ctClass.getConstructors()));
        return Optional.of(toInternalConstructorDefinition(constructor, resolver));
    }
}
