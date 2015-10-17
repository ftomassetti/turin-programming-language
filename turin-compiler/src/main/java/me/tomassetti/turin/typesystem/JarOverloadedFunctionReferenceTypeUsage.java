package me.tomassetti.turin.typesystem;

import javassist.CtMethod;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.compiler.errorhandling.SemanticErrorException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.analysis.resolvers.compiled.JavassistBasedMethodResolution;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JarOverloadedFunctionReferenceTypeUsage extends OverloadedFunctionReferenceTypeUsage
{

    private List<CtMethod> ctMethods;
    private String methodName;
    private boolean staticContext;

    public JarOverloadedFunctionReferenceTypeUsage(List<FunctionReferenceTypeUsage> alternatives, List<CtMethod> ctMethods) {
        super(alternatives);
        if (alternatives.size() != ctMethods.size()) {
            throw new IllegalArgumentException();
        }
        methodName = ctMethods.get(0).getName();
        staticContext = Modifier.isStatic(ctMethods.get(0).getModifiers());
        for (CtMethod method : ctMethods) {
            if (!method.getName().equals(methodName)) {
                throw new IllegalArgumentException();
            }
            if (Modifier.isStatic(method.getModifiers()) != staticContext) {
                throw new IllegalArgumentException();
            }
        }
        this.ctMethods = ctMethods;
    }

    @Override
    public boolean sameType(TypeUsage other, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver) {
        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType().jvmType(resolver));
            }
        }
        CtMethod method = JavassistBasedMethodResolution.findMethodAmong(methodName, argsTypes, resolver, staticContext, ctMethods);
        int index = ctMethods.indexOf(method);
        if (index == -1) {
            throw new RuntimeException();
        }
        return alternatives.get(index).returnTypeWhenInvokedWith(actualParams, resolver);
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
    }

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmType jvmType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

}
