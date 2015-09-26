package me.tomassetti.turin.parser.ast.expressions;

import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Invokable extends Expression {
    protected List<ActualParam> actualParams;

    public List<ActualParam> getActualParams() {
        return actualParams;
    }

    public Invokable(List<ActualParam> actualParams) {
        this.actualParams = new ArrayList<>();
        this.actualParams.addAll(actualParams);
        this.actualParams.forEach((p) ->p.setParent(this));
    }

    public abstract boolean isOnOverloaded(SymbolResolver resolver);

    @Override
    public boolean validate(SymbolResolver resolver, ErrorCollector errorCollector) {
        boolean otherParams = actualParams.stream().filter((p)->!p.isAsterisk()).findFirst().isPresent();
        List<ActualParam> asterisks = actualParams.stream().filter((p)->p.isAsterisk()).collect(Collectors.toList());
        if (asterisks.size() > 1) {
            for (ActualParam actualParam : asterisks.subList(1, asterisks.size())) {
                errorCollector.recordSemanticError(actualParam.getPosition(), "Only one asterisk parameter can be used");
            }
            return false;
        }
        if (asterisks.size() > 0 && otherParams) {
            errorCollector.recordSemanticError(asterisks.get(0).getPosition(), "Asterisk parameter can be used only alone");
            return false;
        }
        if (asterisks.size() > 0 && isOnOverloaded(resolver)) {
            errorCollector.recordSemanticError(asterisks.get(0).getPosition(), "Asterisk parameter cannot be used on overloaded methods");
            return false;
        }
        return super.validate(resolver, errorCollector);
    }

    /**
     * Return a list of param values in order (named param permits to be out of order)
     */
    public List<Expression> getActualParamValuesInOrder() {
        List<Expression> values = new LinkedList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.getName() != null) {
                throw new UnsupportedOperationException();
            }
            values.add(actualParam.getValue());
        }
        return values;
    }
}
