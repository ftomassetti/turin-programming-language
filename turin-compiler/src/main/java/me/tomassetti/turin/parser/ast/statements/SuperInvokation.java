package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

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
        return getTurinTypeDefinition().hasManyConstructors(resolver);
    }

    @Override
    protected List<FormalParameter> formalParameters(SymbolResolver resolver) {
        return getTurinTypeDefinition().getSuperclass(resolver).getConstructorParams(actualParams, resolver);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(actualParams);
    }


    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public Optional<JvmConstructorDefinition> findJvmDefinition(SymbolResolver resolver) {
        return getTurinTypeDefinition().getSuperclass(resolver).findConstructorDefinition(actualParams, resolver);
    }
}

