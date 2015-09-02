package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeUsage;

import java.util.List;

/**
 * Created by federico on 01/09/15.
 */
public class FieldAccess extends Expression {

    private Expression subject;
    private String field;

    public FieldAccess(Expression subject, String field) {
        this.subject = subject;
        this.field = field;
    }

    @Override
    public String toString() {
        return "FieldAccess{" +
                "subject=" + subject +
                ", field='" + field + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldAccess that = (FieldAccess) o;

        if (!field.equals(that.field)) return false;
        if (!subject.equals(that.subject)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + field.hashCode();
        return result;
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        return subject.calcType(resolver).findMethodFor(this.field, argsTypes, resolver, staticContext);
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(subject);
    }
}
