package me.tomassetti.turin.parser.analysis.exceptions;

import me.tomassetti.turin.parser.ast.expressions.Invokable;

public class UnsolvedInvokableException extends UnsolvedException {

    public UnsolvedInvokableException(Invokable invokable) {
        super("Unsolved invokable call " + invokable.toString());
    }

}
