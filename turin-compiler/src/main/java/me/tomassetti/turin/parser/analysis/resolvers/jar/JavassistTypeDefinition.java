package me.tomassetti.turin.parser.analysis.resolvers.jar;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import me.tomassetti.turin.compiler.SemanticErrorException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavassistTypeDefinition extends TypeDefinition {

    private CtClass ctClass;

    public JavassistTypeDefinition(CtClass ctClass) {
        super(ctClass.getSimpleName());
        this.ctClass = ctClass;
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, Resolver resolver, boolean staticContext) {
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
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        try {
            return JavassistTypeDefinitionFactory.toMethodDefinition(JavassistBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, Arrays.asList(ctClass.getMethods()), this));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInterface() {
        return ctClass.isInterface();
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }
}
