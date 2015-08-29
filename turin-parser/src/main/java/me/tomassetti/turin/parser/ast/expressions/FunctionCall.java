package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by federico on 29/08/15.
 */
public class FunctionCall extends Expression {

    private String name;
    private List<ActualParam> actualParams;

    public String getName() {
        return name;
    }

    public List<ActualParam> getActualParams() {
        return actualParams;
    }

    @Override
    public String toString() {

        return "FunctionCall{" +
                "name='" + name + '\'' +
                ", actualParams=" + actualParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionCall that = (FunctionCall) o;

        if (!actualParams.equals(that.actualParams)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + actualParams.hashCode();
        return result;
    }

    public FunctionCall(String name, List<ActualParam> actualParams) {
        this.name = name;
        this.actualParams = new ArrayList<>();
        this.actualParams.addAll(actualParams);
        this.actualParams.forEach((p) ->p.setParent(FunctionCall.this));
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(actualParams);
    }
}
