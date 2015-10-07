package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

public class SuperInvokation extends Statement {

    private List<ActualParam> params;

    public List<ActualParam> getParams() {
        return params;
    }

    @Override

    public String toString() {
        return "SuperInvokation{" +
                "params=" + params +
                '}';
    }

    public SuperInvokation(List<ActualParam> params) {
        this.params = params;
        this.params.forEach((p)->p.setParent(SuperInvokation.this));
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(params);
    }

}

