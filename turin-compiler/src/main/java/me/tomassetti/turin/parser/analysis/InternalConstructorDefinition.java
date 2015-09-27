package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

public class InternalConstructorDefinition extends InternalInvokableDefinition {

    private TurinTypeDefinition turinTypeDefinition;

    public InternalConstructorDefinition(TurinTypeDefinition turinTypeDefinition, List<FormalParameter> formalParameters) {
        super(formalParameters);
        this.turinTypeDefinition = turinTypeDefinition;
    }

    private List<FormalParameter> getFormalParameters(SymbolResolver resolver, List<ActualParam> actualParams) {
        return turinTypeDefinition.getConstructorParams(actualParams, resolver);
    }

}
