package me.tomassetti.turin.parser.ast.typeusage;

import javassist.CtMethod;
import me.tomassetti.turin.compiler.errorhandling.SemanticErrorException;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.analysis.resolvers.compiled.JavassistBasedMethodResolution;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class JarOverloadedFunctionReferenceTypeUsage extends OverloadedFunctionReferenceTypeUsage {

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
    public TypeUsageNode copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsageNode returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver) {
        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType(resolver).jvmType(resolver));
            }
        }
        CtMethod method = JavassistBasedMethodResolution.findMethodAmong(methodName, argsTypes, resolver, staticContext, ctMethods, this);
        int index = ctMethods.indexOf(method);
        if (index == -1) {
            throw new RuntimeException();
        }
        return alternatives.get(index).returnTypeWhenInvokedWith(actualParams, resolver);
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

}
