package me.tomassetti.turin.parser.analysis.resolvers.jdk;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ReflectionBasedSetOfOverloadedMethods extends Expression {

    private List<Method> methods;
    private boolean isStatic;
    private Node instance;
    private String name;

    @Override
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        return ReflectionTypeDefinitionFactory.toMethodDefinition(ReflectionBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, methods, this));
    }

    public ReflectionBasedSetOfOverloadedMethods(List<Method> methods, Node instance) {
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

    public Node getInstance() {
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
    public TypeUsageNode calcType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<FormalParameterNode>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        if (invokable instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)invokable;
            Optional<Method> method = ReflectionBasedMethodResolution.findMethodAmongActualParams(name, invokable.getActualParams(), resolver, functionCall.isStatic(resolver), methods, this);
            return Optional.of(ReflectionBasedMethodResolution.formalParameters(method.get(), Collections.emptyMap()));
        }
        throw new UnsupportedOperationException(invokable.getClass().getCanonicalName());
        // return ReflectionTypeDefinitionFactory.toMethodDefinition(ReflectionBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, methods, this));
    }
}
