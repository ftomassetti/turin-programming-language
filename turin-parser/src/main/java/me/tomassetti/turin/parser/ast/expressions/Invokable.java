package me.tomassetti.turin.parser.ast.expressions;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by federico on 29/08/15.
 */
public abstract class Invokable extends Expression {
    protected List<ActualParam> actualParams;

    public List<ActualParam> getActualParams() {
        return actualParams;
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
