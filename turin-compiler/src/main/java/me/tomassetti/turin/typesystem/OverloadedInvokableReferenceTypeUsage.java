package me.tomassetti.turin.typesystem;

import javassist.CtMethod;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.compiler.errorhandling.SemanticErrorException;
import me.tomassetti.turin.definitions.InternalInvokableDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.compiled.JavassistBasedMethodResolution;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.Symbol;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OverloadedInvokableReferenceTypeUsage extends OverloadedFunctionReferenceTypeUsage {

    private List<CtMethod> ctMethods;
    private String methodName;
    private boolean staticContext;
    private SymbolResolver resolver;

    public OverloadedInvokableReferenceTypeUsage(List<InvokableReferenceTypeUsage> alternatives,
                                                 List<CtMethod> ctMethods,
                                                 SymbolResolver resolver) {
        super(alternatives);
        if (alternatives.size() != ctMethods.size()) {
            throw new IllegalArgumentException();
        }
        this.resolver = resolver;
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
    public boolean sameType(TypeUsage other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType().jvmType());
            }
        }
        CtMethod method = JavassistBasedMethodResolution.findMethodAmong(methodName, argsTypes, resolver, staticContext, ctMethods);
        int index = ctMethods.indexOf(method);
        if (index == -1) {
            throw new RuntimeException();
        }
        return alternatives.get(index).returnTypeWhenInvokedWith(actualParams);
    }

    @Override
    public boolean isOverloaded() {
        return true;
    }

    @Override
    public Optional<InternalInvokableDefinition> internalInvokableDefinitionFor(List<ActualParam> actualParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmType jvmType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException();
    }

}
