package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.compiler.AmbiguousCallException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionBasedSetOfOverloadedMethods extends Node {

    private List<Method> methods;
    private boolean isStatic;
    private Node instance;

    public ReflectionBasedSetOfOverloadedMethods(List<Method> methods, Node instance) {
        if (methods.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.isStatic = Modifier.isStatic(methods.get(0).getModifiers());
        for (Method method : methods) {
            if (isStatic != Modifier.isStatic(methods.get(0).getModifiers())) {
                throw new IllegalArgumentException("All methods should be static or non static");
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
}
