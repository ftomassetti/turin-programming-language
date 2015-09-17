package me.tomassetti.turin.parser.ast.expressions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
