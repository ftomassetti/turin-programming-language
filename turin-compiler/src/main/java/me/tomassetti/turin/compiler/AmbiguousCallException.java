package me.tomassetti.turin.compiler;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public class AmbiguousCallException extends RuntimeException {

    public AmbiguousCallException(Node context, String name, List<JvmType> paramList) {

    }

}
