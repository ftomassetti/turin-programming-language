package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.TypeUsage;

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
        return "InstanceFieldAccess{" +
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

    public boolean isArrayLength(SymbolResolver resolver) {
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
    public TypeUsage calcType() {
        return subject.getField(field, symbolResolver()).calcType();
    }

    @Override
    public boolean canBeAssigned(SymbolResolver resolver) {
        return subject.canFieldBeAssigned(field, resolver);
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(subject);
    }
}
