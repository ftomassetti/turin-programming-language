package me.tomassetti.turin.parser.analysis.exceptions;

import me.tomassetti.turin.parser.ast.expressions.InvokableExpr;

public class UnsolvedInvokableException extends UnsolvedException {

    public UnsolvedInvokableException(InvokableExpr invokable) {
        super("Unsolved invokable call " + invokable.toString());
    }

}
