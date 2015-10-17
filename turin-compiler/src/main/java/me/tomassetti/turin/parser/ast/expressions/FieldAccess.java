package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;

import java.util.List;
import java.util.Optional;

public class FieldAccess extends Expression {

    private Expression subject;
    private String field;

    public Expression getSubject() {
        return subject;
    }

    public String getField() {
        return field;
    }

    public FieldAccess(Expression subject, String field) {
        this.subject = subject;
        this.subject.setParent(this);
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
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        return subject.calcType().findMethodFor(this.field, argsTypes, resolver, staticContext);
    }

    @Override
    public TypeUsageNode calcType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(subject);
    }

    @Override
    public Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        if (invokable instanceof Creation) {
            //return Optional.of(typeDefinition.getConstructorParams(invokable.getActualParams(), resolver));
            throw new UnsupportedOperationException();
        } else if (invokable instanceof FunctionCall) {
            return subject.calcType().findFormalParametersFor(invokable, resolver);
        } else {
            throw new UnsupportedOperationException(invokable.getClass().getCanonicalName());
        }
    }
}
