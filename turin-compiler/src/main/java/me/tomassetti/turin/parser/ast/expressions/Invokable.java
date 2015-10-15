package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.ParamUtils;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedInvokableException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.TypeUsage;
import me.tomassetti.turin.util.Either;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Invokable extends Expression {
    protected List<ActualParam> actualParams;

    public List<ActualParam> getActualParams() {
        return actualParams;
    }

    public Invokable(List<ActualParam> actualParams) {
        this.actualParams = new ArrayList<>();
        this.actualParams.addAll(actualParams);
        this.actualParams.forEach((p) ->p.setParent(this));
        originalParams = actualParams;
    }

    public void desugarize(SymbolResolver resolver) {
        if (desugarized) {
            return;
        }
        concreteDesugarize(resolver);
        desugarized = true;
    }

    private boolean desugarized = false;
    protected List<ActualParam> originalParams;
    public abstract boolean isOnOverloaded(SymbolResolver resolver);

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        boolean otherParams = actualParams.stream().filter((p)->!p.isAsterisk()).findFirst().isPresent();
        List<ActualParam> asterisks = actualParams.stream().filter((p)->p.isAsterisk()).collect(Collectors.toList());
        if (asterisks.size() > 1) {
            for (ActualParam actualParam : asterisks.subList(1, asterisks.size())) {
                errorCollector.recordSemanticError(actualParam.getPosition(), "Only one asterisk parameter can be used");
            }
            return false;
        }
        if (asterisks.size() > 0 && otherParams) {
            errorCollector.recordSemanticError(asterisks.get(0).getPosition(), "Asterisk parameter can be used only alone");
            return false;
        }
        if (asterisks.size() > 0 && isOnOverloaded(resolver)) {
            errorCollector.recordSemanticError(asterisks.get(0).getPosition(), "Asterisk parameter cannot be used on overloaded methods");
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    /**
     * Return a list of param values in order (named param permits to be out of order)
     */
    public List<Expression> getActualParamValuesInOrder() {
        List<Expression> values = new LinkedList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.getName() != null) {
                throw new UnsupportedOperationException();
            }
            values.add(actualParam.getValue());
        }
        return values;
    }

    protected abstract List<FormalParameter> formalParameters(SymbolResolver resolver);

    protected final List<FormalParameter> defaultParameters(SymbolResolver resolver) {
        return formalParameters(resolver).stream().filter((p)->p.hasDefaultValue()).collect(Collectors.toList());
    }

    protected final boolean hasDefaultParameters(SymbolResolver resolver) {
        return !defaultParameters(resolver).isEmpty();
    }

    protected final boolean hasAsteriskActualParameter() {
        return originalParams.stream().filter((p)->p.isAsterisk()).findFirst().isPresent();
    }

    private void concreteDesugarize(SymbolResolver resolver) {
        // all named parameters should be after the named ones
        if (!ParamUtils.verifyOrder(actualParams)) {
            throw new IllegalArgumentException("Named params should all be grouped after the positional ones:" + actualParams);
        }
        if (hasAsteriskActualParameter()){
            concreteDesugarizeWithAsterisk(resolver);
        } else {
            concreteDesugarizeWithoutAsterisk(resolver);
        }
    }

    private void concreteDesugarizeWithAsterisk(SymbolResolver resolver) {
        if (actualParams.size() != 1) {
            throw new IllegalStateException();
        }
        ActualParam asteriskParam = actualParams.get(0);

        Map<String, ActualParam> paramsAssigned = new HashMap<>();

        List<FormalParameter> formalParams = formalParameters(resolver);
        Either<String, List<ActualParam>> res = ParamUtils.desugarizeAsteriskParam(formalParams, asteriskParam.getValue(), resolver, this);
        if (res.isLeft()) {
            throw new IllegalArgumentException(res.getLeft());
        }
        actualParams = res.getRight();
    }

    private void concreteDesugarizeWithoutAsterisk(SymbolResolver resolver) {
        Map<String, ActualParam> paramsAssigned = new HashMap<>();

        List<FormalParameter> formalParams = formalParameters(resolver);
        formalParams.forEach((fp)->fp.setParent(this));
        List<ActualParam> unnamedParams = ParamUtils.unnamedParams(actualParams);
        List<ActualParam> namedParams = ParamUtils.namedParams(actualParams);

        // use the unnamed params
        if (unnamedParams.size() > formalParams.size()) {
            throw new IllegalArgumentException("Too many unnamed params: " + actualParams +". Formal params are: " + formalParams);
        }
        int i = 0;
        for (ActualParam param : unnamedParams) {
            if (formalParams.get(i).getParent() == null) {
                throw new IllegalStateException();
            }
            TypeUsage actualParamType = param.getValue().calcType(resolver);
            TypeUsageNode formalParamType = formalParams.get(i).getType();
            if (!actualParamType.canBeAssignedTo(formalParamType, resolver)){
                throw new UnsolvedInvokableException(this);
            }
            paramsAssigned.put(formalParams.get(i).getName(), param);
            i++;
        }
        // use the named params
        Map<String, FormalParameter> validNames = new HashMap<>();
        formalParams.forEach((p) -> validNames.put(p.getName(), p));
        for (ActualParam param : namedParams) {
            if (paramsAssigned.containsKey(param.getName())) {
                throw new IllegalArgumentException("Param " + param.getName() + " assigned several times");
            }
            if (!validNames.containsKey(param.getName())) {
                throw new IllegalArgumentException("Unknown param " + param.getName());
            }
            if (!param.getValue().calcType(resolver).canBeAssignedTo(validNames.get(param.getName()).getType(), resolver)){
                throw new UnsolvedInvokableException(this);
            }
            paramsAssigned.put(param.getName(), param);
        }

        // all parameters have been assigned
        for (FormalParameter formalParameter : formalParams) {
            if (!paramsAssigned.containsKey(formalParameter.getName()) && !formalParameter.hasDefaultValue()) {
                throw new IllegalArgumentException("Param not assigned: " + formalParameter.getName());
            }
        }

        List<ActualParam> orderedParams = new ArrayList<>();
        for (FormalParameter formalParameter : formalParams) {
            if (!formalParameter.hasDefaultValue()) {
                ActualParam actualParam = paramsAssigned.get(formalParameter.getName());
                if (actualParam.isNamed()) {
                    actualParam = actualParam.toUnnamed();
                    actualParam.setParent(this);
                }
                orderedParams.add(actualParam);
            }
        }
        // add the map with the default params
        if (hasDefaultParameters(resolver)) {
            Expression mapCreation = new Creation("turin.collections.MapBuilder", Collections.emptyList());
            for (FormalParameter formalParameter : defaultParameters(resolver)) {
                if (paramsAssigned.containsKey(formalParameter.getName())) {
                    List<ActualParam> params = new ArrayList<>();
                    params.add(new ActualParam(new StringLiteral(formalParameter.getName())));
                    params.add(new ActualParam(paramsAssigned.get(formalParameter.getName()).getValue()));
                    mapCreation = new InstanceMethodInvokation(mapCreation, "put", params);
                }
            }
            mapCreation = new InstanceMethodInvokation(mapCreation, "build", ImmutableList.of());
            mapCreation.setParent(this);
            ActualParam mapForDefaultParams = new ActualParam(mapCreation);
            mapForDefaultParams.setParent(this);
            orderedParams.add(mapForDefaultParams);
        }
        actualParams = orderedParams;
    }
}
