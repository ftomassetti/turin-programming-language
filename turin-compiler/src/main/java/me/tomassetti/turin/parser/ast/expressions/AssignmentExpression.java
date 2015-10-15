package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

public class AssignmentExpression extends Expression {

    private Expression target;
    private Expression value;

    @Override
    public String toString() {
        return "AssignmentStatement{" +
                "target=" + target +
                ", value=" + value +
                '}';
    }

    public AssignmentExpression(Expression target, Expression value) {
        this.target = target;
        this.target.setParent(this);
        this.value = value;
        this.value.setParent(this);
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        if (!this.target.canBeAssigned(resolver)) {
            errorCollector.recordSemanticError(this.target.getPosition(), "Cannot be assigned");
            return false;
        }

        return super.specificValidate(resolver, errorCollector);
    }

    public Expression getTarget() {
        return target;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(target, value);
    }

    @Override
    public TypeUsageNode calcType(SymbolResolver resolver) {
        return getTarget().calcType(resolver);
    }
}
