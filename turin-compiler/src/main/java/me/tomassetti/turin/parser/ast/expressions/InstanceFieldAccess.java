package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmFieldDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

public class InstanceFieldAccess extends Expression {

    private Expression subject;
    private String field;

    public InstanceFieldAccess(Expression subject, String field) {
        this.subject = subject;
        this.subject.setParent(this);
        this.field = field;
    }

    @Override
    public String toString() {
        return "StaticFieldAccess{" +
                "subject=" + subject +
                ", field='" + field + '\'' +
                '}';
    }

    public Expression getSubject() {
        return subject;
    }

    public String getField() {
        return field;
    }

    public boolean isArrayLength(Resolver resolver) {
        return getSubject().calcType(resolver).isArray() && field.equals("length");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceFieldAccess that = (InstanceFieldAccess) o;

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
    public TypeUsage calcType(Resolver resolver) {
        return subject.getField(field, resolver).calcType(resolver);
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(subject);
    }

    public JvmFieldDefinition toJvmField(Resolver resolver) {
        throw new UnsupportedOperationException();
    }
}
