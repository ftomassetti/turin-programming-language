package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;


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
        super(actualParams);
        this.function = name;
        this.function.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().add(function).addAll(actualParams).build();
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        return function.calcType(resolver).returnTypeWhenInvokedWith(actualParams);
    }

}
