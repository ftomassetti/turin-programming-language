package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.ParamUtils;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.UnsolvedConstructorException;
import me.tomassetti.turin.parser.analysis.UnsolvedTypeException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.*;


public class Creation extends Invokable {

    private String typeName;

    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return "Creation{" +
                "typeName='" + typeName + '\'' +
                ", actualParams=" + actualParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Creation creation = (Creation) o;

        if (!actualParams.equals(creation.actualParams)) return false;
        if (!typeName.equals(creation.typeName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = typeName.hashCode();
        result = 31 * result + actualParams.hashCode();
        return result;
    }

    public Creation(String typeName, List<ActualParam> actualParams) {
        super(actualParams);
        originalParams = actualParams;
        this.typeName = typeName;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(actualParams);
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        // this node will not have a context so we resolve the type already
        Optional<TypeUsage> typeUsage = resolver.findTypeUsageIn(typeName, this, resolver);
        if (!typeUsage.isPresent()) {
            throw new UnsolvedTypeException(typeName, this);
        }
        return typeUsage.get();
    }

    public JvmConstructorDefinition jvmDefinition(SymbolResolver resolver) {
        return resolver.getTypeDefinitionIn(typeName, this, resolver).resolveConstructorCall(resolver, originalParams);
    }

    public void desugarize(SymbolResolver resolver) {
        if (desugarized) {
            return;
        }
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(typeName, this, resolver);
        if (typeDefinition instanceof TurinTypeDefinition) {
            TurinTypeDefinition turinTypeDefinition = (TurinTypeDefinition)typeDefinition;
            desugarizeCreationOfTurinType(resolver, turinTypeDefinition);
        }
        desugarized = true;
    }

    private boolean desugarized = false;
    private List<ActualParam> originalParams;

    private void desugarizeCreationOfTurinType(SymbolResolver resolver, TurinTypeDefinition turinType) {
        // all named parameters should be after the named ones
        if (!ParamUtils.allNamedParamsAreAtTheEnd(actualParams)) {
            throw new IllegalArgumentException("Named params should all be grouped after the positional ones");
        }

        Map<String, ActualParam> paramsAssigned = new HashMap<>();

        List<Property> propertiesWhichCanBeAssignedWithoutName = turinType.propertiesWhichCanBeAssignedWithoutName(resolver);
        List<ActualParam> unnamedParams = ParamUtils.unnamedParams(actualParams);
        List<ActualParam> namedParams = ParamUtils.namedParams(actualParams);

        // use the unnamed params
        if (unnamedParams.size() > turinType.propertiesWhichCanBeAssignedWithoutName(resolver).size()) {
            throw new IllegalArgumentException("Too many unnamed params: " + actualParams);
        }
        int i = 0;
        for (ActualParam param : unnamedParams) {
            if (!param.getValue().calcType(resolver).canBeAssignedTo(propertiesWhichCanBeAssignedWithoutName.get(i).getTypeUsage(), resolver)){
                throw new UnsolvedConstructorException(turinType.getQualifiedName(), actualParams);
            }
            paramsAssigned.put(propertiesWhichCanBeAssignedWithoutName.get(i).getName(), param);
            i++;
        }
        // use the named params
        Map<String, Property> validNames = new HashMap<>();
        turinType.assignableProperties(resolver).forEach((p) ->validNames.put(p.getName(), p));
        for (ActualParam param : namedParams) {
            if (paramsAssigned.containsKey(param.getName())) {
                throw new IllegalArgumentException("Property " + param.getName() + " assigned several times");
            }
            if (!validNames.containsKey(param.getName())) {
                throw new IllegalArgumentException("Unknown property " + param.getName());
            }
            if (!param.getValue().calcType(resolver).canBeAssignedTo(validNames.get(param.getName()).getTypeUsage(), resolver)){
                throw new UnsolvedConstructorException(turinType.getQualifiedName(), actualParams);
            }
            paramsAssigned.put(param.getName(), param);
        }

        // verify that all properties with no default or initial value have been assigned
        for (Property property : turinType.propertiesAppearingInConstructor(resolver)) {
            if (!paramsAssigned.containsKey(property.getName())) {
                throw new IllegalArgumentException("Property not assigned: " + property.getName());
            }
        }

        List<ActualParam> orderedParams = new ArrayList<>();
        for (Property property : turinType.propertiesAppearingInConstructor(resolver)) {
            ActualParam actualParam = paramsAssigned.get(property.getName());
            if (actualParam.isNamed()) {
                actualParam = actualParam.toUnnamed();
                actualParam.setParent(this);
            }
            orderedParams.add(actualParam);
        }
        // add the map with the default params
        if (turinType.hasDefaultProperties(resolver)) {
            Expression mapCreation = new Creation("turin.collections.MapBuilder", Collections.emptyList());
            for (Property property : turinType.defaultPropeties(resolver)) {
                List<ActualParam> params = new ArrayList<>();
                params.add(new ActualParam(new StringLiteral(property.getName())));
                if (paramsAssigned.containsKey(property.getName())) {
                    params.add(new ActualParam(paramsAssigned.get(property.getName()).getValue()));
                } else {
                    params.add(new ActualParam(property.getDefaultValue().get()));
                }
                mapCreation = new InstanceMethodInvokation(mapCreation, "put", params);
            }
            mapCreation = new InstanceMethodInvokation(mapCreation, "build", ImmutableList.of());
            ActualParam mapForDefaultParams = new ActualParam(mapCreation);
            mapForDefaultParams.setParent(this);
            orderedParams.add(mapForDefaultParams);
        }
        actualParams = orderedParams;
    }
}
