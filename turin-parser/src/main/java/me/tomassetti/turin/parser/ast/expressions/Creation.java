package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeUsage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by federico on 29/08/15.
 */
public class Creation extends Expression {

    private String typeName;
    private List<ActualParam> actualParams;

    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return "Creation{" +
                "typeName='" + typeName + '\'' +
                ", actualParams=" + actualParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Creation creation = (Creation) o;

        if (!actualParams.equals(creation.actualParams)) return false;
        if (!typeName.equals(creation.typeName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = typeName.hashCode();
        result = 31 * result + actualParams.hashCode();
        return result;
    }

    public List<ActualParam> getActualParams() {

        return actualParams;
    }

    public Creation(String typeName, List<ActualParam> actualParams) {
        this.typeName = typeName;
        this.actualParams = actualParams;
        this.actualParams = new ArrayList<>();
        this.actualParams.addAll(actualParams);
        this.actualParams.forEach((p) ->p.setParent(Creation.this));
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(actualParams);
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        throw new UnsupportedOperationException();
    }
}
