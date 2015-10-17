package me.tomassetti.turin.definitions;

import me.tomassetti.turin.compiler.ParamUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.*;

public abstract class InternalInvokableDefinition {

    public boolean match(SymbolResolver resolver, List<ActualParam> actualParams) {
        // all named parameters should be after the named ones
        if (!ParamUtils.verifyOrder(actualParams)) {
            throw new IllegalArgumentException("Named params should all be grouped after the positional ones");
        }
        if (actualParams.stream().filter((p)->p.isAsterisk()).findFirst().isPresent()) {
            if (actualParams.size() > 1) {
                throw new IllegalArgumentException("Too many params");
            }
            return !matchAsterisk(resolver, actualParams.get(0), getFormalParameters()).isPresent();
        } else {
            return !matchNotAsterisk(resolver, actualParams).isPresent();
        }
    }

    private Optional<String> matchAsterisk(SymbolResolver resolver, ActualParam actualParam, List<? extends FormalParameter> formalParameters) {
        TypeUsage paramType = actualParam.getValue().calcType();
        // it needs to have all the getters for the non-default parameters
        // all the other getters that match default params have to be the right type

        if (!paramType.isReference()) {
            return Optional.of("An asterisk param should be an object");
        }
        List<ActualParam> actualParams = new ArrayList<>();
        TypeDefinition typeDefinition = paramType.asReferenceTypeUsage().getTypeDefinition();

        for (FormalParameter formalParameter : formalParameters) {
            String getterName = ParamUtils.getterName(formalParameter, resolver);
            if (typeDefinition.hasMethodFor(getterName, Collections.emptyList(), resolver, false)) {
                TypeUsage res = typeDefinition.returnTypeWhenInvokedWith(getterName, Collections.emptyList(), resolver, false);
                if (!res.canBeAssignedTo(formalParameter.getType(), resolver)){
                    return Optional.of("the given value has a getter '" + getterName + "' with incompatible type");
                }
            } else {
                if (!formalParameter.hasDefaultValue()) {
                    return Optional.of("the given value has not a getter '" + getterName + "'");
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> matchNotAsterisk(SymbolResolver resolver, List<ActualParam> actualParams) {
        Set<String> paramsAssigned = new HashSet<>();

        List<? extends FormalParameter> formalParameters = getFormalParameters();
        List<ActualParam> unnamedParams = ParamUtils.unnamedParams(actualParams);
        List<ActualParam> namedParams = ParamUtils.namedParams(actualParams);

        // use the unnamed params
        if (unnamedParams.size() > formalParameters.size()) {
            return Optional.of("Too many unnamed params: " + actualParams);
        }
        int i = 0;
        for (ActualParam param : unnamedParams) {
            if (!param.getValue().calcType().canBeAssignedTo(formalParameters.get(i).getType(), resolver)){
                return Optional.of("TODO");
            }
            paramsAssigned.add(formalParameters.get(i).getName());
            i++;
        }
        // use the named params
        Map<String, FormalParameter> validNames = new HashMap<>();
        formalParameters.forEach((p) -> validNames.put(p.getName(), p));
        for (ActualParam param : namedParams) {
            if (paramsAssigned.contains(param.getName())) {
                return Optional.of("Param " + param.getName() + " assigned several times");
            }
            if (!validNames.containsKey(param.getName())) {
                return Optional.of("Unknown param " + param.getName());
            }
            if (!param.getValue().calcType().canBeAssignedTo(validNames.get(param.getName()).getType(), resolver)){
                return Optional.of("TODO");
            }
            paramsAssigned.add(param.getName());
        }

        // verify that all properties with no default or initial value have been assigned
        for (FormalParameter formalParameter : formalParameters) {
            if (!paramsAssigned.contains(formalParameter.getName()) && !formalParameter.hasDefaultValue()) {
                return Optional.of("Param not assigned: " + formalParameter.getName());
            }
        }
        return Optional.empty();
    }

    private List<? extends FormalParameter> formalParameters;

    public InternalInvokableDefinition(List<? extends FormalParameter> formalParameters) {
        this.formalParameters = formalParameters;
    }

    public List<? extends FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    public boolean matchJvmTypes(SymbolResolver resolver, List<JvmType> jvmTypes) {
        List<? extends FormalParameter> formalParameters = getFormalParameters();
        if (formalParameters.size() != jvmTypes.size()) {
            return false;
        }
        for (int i=0;i<jvmTypes.size();i++) {
            FormalParameter formalParameter = formalParameters.get(i);
            JvmType jvmType = jvmTypes.get(i);
            if (!formalParameter.getType().jvmType().isAssignableBy(jvmType)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasDefaultParams() {
        return getFormalParameters().stream().filter((p)->p.hasDefaultValue()).findFirst().isPresent();
    }
}
