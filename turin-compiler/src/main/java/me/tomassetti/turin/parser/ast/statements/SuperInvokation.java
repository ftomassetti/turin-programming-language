package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;

import java.util.List;
import java.util.Optional;

public class SuperInvokation extends Invokable {

    @Override
    public String toString() {
        return "SuperInvokation{" +
                "params=" + actualParams +
                '}';
    }

    private TurinTypeDefinition getTurinTypeDefinition() {
        return getParentOfType(TurinTypeDefinition.class);
    }

    public SuperInvokation(List<ActualParam> params) {
        super(params);
    }

    @Override
    public boolean isOnOverloaded(SymbolResolver resolver) {
        return getTurinTypeDefinition().hasManyConstructors();
    }

    @Override
    protected List<? extends FormalParameter> formalParameters(SymbolResolver resolver) {
        return getTurinTypeDefinition().getSuperclass().getConstructorParams(actualParams);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(actualParams);
    }


    @Override
    public TypeUsageNode calcType() {
        throw new UnsupportedOperationException();
    }

    public Optional<JvmConstructorDefinition> findJvmDefinition(SymbolResolver resolver) {
        return getTurinTypeDefinition().getSuperclass().findConstructorDefinition(actualParams);
    }
}

