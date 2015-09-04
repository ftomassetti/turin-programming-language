package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by federico on 29/08/15.
 */
public class FunctionCall extends Invokable {

    private Expression function;

    public Expression getFunction() {
        return function;
    }

    @Override
    public String toString() {

        return "FunctionCall{" +
                "function='" + function + '\'' +
                ", actualParams=" + actualParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionCall that = (FunctionCall) o;

        if (!actualParams.equals(that.actualParams)) return false;
        if (!function.equals(that.function)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = function.hashCode();
        result = 31 * result + actualParams.hashCode();
        return result;
    }

    public FunctionCall(Expression name, List<ActualParam> actualParams) {
        this.function = name;
        this.actualParams = new ArrayList<>();
        this.actualParams.addAll(actualParams);
        this.actualParams.forEach((p) ->p.setParent(FunctionCall.this));
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(actualParams);
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    public String jvmSignature(Resolver resolver) {
        throw new UnsupportedOperationException();
    }
}
