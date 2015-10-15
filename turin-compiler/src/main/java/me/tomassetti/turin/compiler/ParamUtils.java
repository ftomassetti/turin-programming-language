package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Creation;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.InstanceMethodInvokation;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.TypeUsage;
import me.tomassetti.turin.util.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ParamUtils {

    private ParamUtils() {
        // prevent instantiation
    }

    public static boolean verifyOrder(List<ActualParam> actualParams) {
        boolean findNamed = false;
        for (ActualParam actualParam : actualParams) {
            if (findNamed && (!actualParam.isNamed() && !actualParam.isAsterisk())) {
                return false;
            }
            findNamed = findNamed || actualParam.isNamed();
        }
        return true;
    }

    public static List<ActualParam> unnamedParams(List<ActualParam> actualParams) {
       return actualParams.stream().filter((p)->!p.isNamed() && !p.isAsterisk()).collect(Collectors.toList());
    }

    public static List<ActualParam> namedParams(List<ActualParam> actualParams) {
        return actualParams.stream().filter((p) -> p.isNamed()).collect(Collectors.toList());
    }

    public static boolean hasDefaultParams(List<FormalParameter> formalParameters) {
        return formalParameters.stream().filter((p)->p.hasDefaultValue()).findFirst().isPresent();
    }

    public static Either<String, List<ActualParam>> desugarizeAsteriskParam(List<FormalParameter> formalParameters, Expression value, SymbolResolver resolver, Node parent) {
        TypeUsage type = value.calcType(resolver);
        if (!type.isReference()) {
            return Either.left("An asterisk param should be an object");
        }
        List<ActualParam> actualParams = new ArrayList<>();
        TypeDefinition typeDefinition = type.asReferenceTypeUsage().getTypeDefinition(resolver);

        // map with the default params
        Expression mapCreation = new Creation("turin.collections.MapBuilder", Collections.emptyList());
        mapCreation.setParent(parent);

        for (FormalParameter formalParameter : formalParameters) {
            String getterName = getterName(formalParameter);
            InstanceMethodInvokation instanceMethodInvokation = new InstanceMethodInvokation(value, getterName, Collections.emptyList());
            if (typeDefinition.hasMethodFor(getterName, Collections.emptyList(), resolver, false)) {
                if (formalParameter.hasDefaultValue()) {
                    List<ActualParam> params = new ArrayList<>();
                    params.add(new ActualParam(new StringLiteral(formalParameter.getName())));
                    params.add(new ActualParam(instanceMethodInvokation));
                    mapCreation = new InstanceMethodInvokation(mapCreation, "put", params);
                } else {
                    ActualParam actualParam = new ActualParam(instanceMethodInvokation);
                    actualParam.setParent(parent);
                    actualParams.add(actualParam);
                }
            } else {
                if (!formalParameter.hasDefaultValue()) {
                    return Either.left("the given value has not a getter '" + getterName + "'");
                }
            }
        }

        if (hasDefaultParams(formalParameters)) {
            mapCreation = new InstanceMethodInvokation(mapCreation, "build", ImmutableList.of());
            mapCreation.setParent(parent);
            ActualParam mapForDefaultParams = new ActualParam(mapCreation);
            mapForDefaultParams.setParent(parent);
            actualParams.add(mapForDefaultParams);
        }

        return Either.right(actualParams);
    }

    /**
     * Corresponding getter method name
     */
    public static String getterName(FormalParameter formalParameter) {
        return Property.getterName(formalParameter.getType(), formalParameter.getName());
    }
}
