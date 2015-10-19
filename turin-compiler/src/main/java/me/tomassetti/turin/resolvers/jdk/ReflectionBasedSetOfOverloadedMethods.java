package me.tomassetti.turin.resolvers.jdk;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO it should not be a Node
public class ReflectionBasedSetOfOverloadedMethods extends Expression {

    private List<Method> methods;
    private boolean isStatic;
    private Symbol instance;
    private String name;

    @Override
    public JvmMethodDefinition findMethodFor(List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        List<JvmType> argsTypes = actualParams.stream().map((ap)->ap.getValue().calcType().jvmType()).collect(Collectors.toList());
        return ReflectionTypeDefinitionFactory.toMethodDefinition(ReflectionBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, methods));
    }

    private SymbolResolver symbolResolver;

    public ReflectionBasedSetOfOverloadedMethods(List<Method> methods, Symbol instance, SymbolResolver symbolResolver) {
        this.symbolResolver = symbolResolver;
        if (methods.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.isStatic = Modifier.isStatic(methods.get(0).getModifiers());
        this.name = methods.get(0).getName();
        for (Method method : methods) {
            if (isStatic != Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("All methods should be static or non static");
            }
            if (!name.equals(method.getName())) {
                throw new IllegalArgumentException("All methods should be named " + name);
            }
        }
        this.methods = methods;
        this.instance = instance;
    }

    public Symbol getInstance() {
        return instance;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public TypeUsageNode calcType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable) {
        if (invokable instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)invokable;
            Optional<Method> method = ReflectionBasedMethodResolution.findMethodAmongActualParams(name, invokable.getActualParams(), symbolResolver, functionCall.isStatic(), methods);
            return Optional.of(ReflectionBasedMethodResolution.formalParameters(method.get(), Collections.emptyMap(), symbolResolver));
        }
        throw new UnsupportedOperationException(invokable.getClass().getCanonicalName());
        // return ReflectionTypeDefinitionFactory.toMethodDefinition(ReflectionBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, methods, this));
    }
}
