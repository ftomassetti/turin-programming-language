package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeUsage;

/**
 * Created by federico on 01/09/15.
 */
public class StaticFieldAccess extends Expression {

    private TypeIdentifier subject;
    private String field;

    public StaticFieldAccess(TypeIdentifier subject, String field) {
        this.subject = subject;
        this.field = field;
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
