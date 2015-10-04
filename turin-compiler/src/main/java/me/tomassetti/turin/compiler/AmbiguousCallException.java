package me.tomassetti.turin.compiler;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

/**
 * A call is ambiguous: we are not able to differentiate between various overloaded methods to invoke.
 */
public class AmbiguousCallException extends RuntimeException {

    private Node context;
    private String name;
    private List<JvmType> actualParamTypes;
    private List<ActualParam> actualParams;

    public AmbiguousCallException(Node context, List<ActualParam> actualParams, String name) {
        this.context = context;
        this.actualParams = actualParams;
        this.name = name;
    }

    public AmbiguousCallException(Node context, String name, List<JvmType> actualParamTypes) {
        this.context = context;
        this.name = name;
        this.actualParamTypes = actualParamTypes;
    }

    public Node getContext() {
        return context;
    }

    public String getName() {
        return name;
    }

    public List<JvmType> getActualParamTypes() {
        return actualParamTypes;
    }

    public List<ActualParam> getActualParams() {
        return actualParams;
    }

}
