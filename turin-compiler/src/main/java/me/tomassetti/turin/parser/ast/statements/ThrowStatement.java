package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class ThrowStatement extends Statement {

    private Expression exception;

    public ThrowStatement(Expression exception) {
        this.exception = exception;
        this.exception.setParent(this);
    }

    @Override
    public String toString() {
        return "ThrowStatement{" +
                "exception=" + exception +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThrowStatement that = (ThrowStatement) o;

        if (!exception.equals(that.exception)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return exception.hashCode();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(exception);
    }

    public Expression getException() {
        return exception;
    }

    public static final String ERR_MESSAGE = "the throw statement should be used only with Exceptions";

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        TypeUsage exceptionType = getException().calcType(resolver);
        if (!exceptionType.isReference()) {
            errorCollector.recordSemanticError(exception.getPosition(), ERR_MESSAGE);
            return false;
        } else {
            if (exceptionType.asReferenceTypeUsage().canBeAssignedTo(new ReferenceTypeUsage(Exception.class.getCanonicalName()), resolver)) {
                return true;
            } else {
                errorCollector.recordSemanticError(exception.getPosition(), ERR_MESSAGE);
                return false;
            }
        }
    }
}
