package me.tomassetti.turin.compiler;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

/**
 * A call is ambiguous: we are not able to differentiate between various overloaded methods to invoke.
 */
public class AmbiguousCallException extends RuntimeException {

    private Node context;
    private String name;
    private List<JvmType> paramList;

    public AmbiguousCallException(Node context, List<ActualParam> argsTypes, String name) {
        // FIXME
        this.context = context;
        this.name = name;
    }

    public Node getContext() {
        return context;
    }

    public String getName() {
        return name;
    }

    public List<JvmType> getParamList() {
        return paramList;
    }

    public AmbiguousCallException(Node context, String name, List<JvmType> paramList) {
        this.context = context;
        this.name = name;
        this.paramList = paramList;
    }

}
