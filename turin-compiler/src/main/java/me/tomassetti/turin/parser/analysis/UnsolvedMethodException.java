package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.expressions.FunctionCall;

public class UnsolvedMethodException extends UnsolvedException {

    public UnsolvedMethodException(FunctionCall functionCall) {
        super("Unsolved function call " + functionCall.toString());
    }

}
