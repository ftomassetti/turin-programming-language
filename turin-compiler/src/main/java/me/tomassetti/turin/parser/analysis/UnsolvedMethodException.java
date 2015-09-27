package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;

import java.util.List;

public class UnsolvedMethodException extends UnsolvedException {

    public UnsolvedMethodException(FunctionCall functionCall) {
        super("Unsolved function call " + functionCall.toString());
    }

    public UnsolvedMethodException(String qualifiedName, String methodName, List<ActualParam> actualParams) {
        super("Unsolved method " + methodName);
        // TODO improve
    }
}
