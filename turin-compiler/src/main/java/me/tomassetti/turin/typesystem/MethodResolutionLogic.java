package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.compiler.AmbiguousCallException;
import me.tomassetti.turin.definitions.InternalMethodDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodResolutionLogic {

    public static Optional<InternalMethodDefinition> findMethodAmongActualParams(List<ActualParam> argsTypes, List<InternalMethodDefinition> candidates) {
        List<InternalMethodDefinition> suitableMethods = new ArrayList<>();
        for (InternalMethodDefinition method : candidates) {
            if (method.getFormalParameters().size() == argsTypes.size()) {
                boolean match = true;
                for (int i = 0; i < argsTypes.size(); i++) {
                    TypeUsage actualType = argsTypes.get(i).getValue().calcType();
                    TypeUsage formalType = method.getFormalParameters().get(i).getType();
                    if (!actualType.canBeAssignedTo(formalType)) {
                        match = false;
                    }
                }
                if (match) {
                    suitableMethods.add(method);
                }
            }
        }

        if (suitableMethods.size() == 0) {
            return Optional.empty();
        } else if (suitableMethods.size() == 1) {
            return Optional.of(suitableMethods.get(0));
        } else {
            return Optional.of(findMostSpecific(suitableMethods,
                    new AmbiguousCallException(null, argsTypes, ""),
                    argsTypes.stream().map((ap) -> ap.getValue().calcType().jvmType()).collect(Collectors.toList())));
        }
    }

    private static InternalMethodDefinition findMostSpecific(List<InternalMethodDefinition> methods, AmbiguousCallException exceptionToThrow,
                                                        List<JvmType> argsTypes) {
        InternalMethodDefinition winningMethod = methods.get(0);
        for (InternalMethodDefinition other : methods.subList(1, methods.size())) {
            if (isTheFirstMoreSpecific(winningMethod, other, argsTypes)) {
            } else if (isTheFirstMoreSpecific(other, winningMethod, argsTypes)) {
                winningMethod = other;
            } else if (!isTheFirstMoreSpecific(winningMethod, other, argsTypes)) {
                // neither is more specific
                throw exceptionToThrow;
            }
        }
        return winningMethod;
    }

    private static boolean isTheFirstMoreSpecific(InternalMethodDefinition first, InternalMethodDefinition second,
                                                  List<JvmType> argsTypes) {
        boolean atLeastOneParamIsMoreSpecific = false;
        if (first.getFormalParameters().size() != second.getFormalParameters().size()) {
            throw new IllegalArgumentException();
        }
        for (int i=0;i<first.getFormalParameters().size();i++){
            TypeUsage paramFirst = first.getFormalParameters().get(i).getType();
            TypeUsage paramSecond = second.getFormalParameters().get(i).getType();
            if (isTheFirstMoreSpecific(paramFirst, paramSecond, argsTypes.get(i))) {
                atLeastOneParamIsMoreSpecific = true;
            } else if (isTheFirstMoreSpecific(paramSecond, paramFirst, argsTypes.get(i))) {
                return false;
            }
        }

        return atLeastOneParamIsMoreSpecific;
    }

    private static boolean isTheFirstMoreSpecific(TypeUsage firstType, TypeUsage secondType, JvmType targetType) {
        boolean firstIsPrimitive = firstType.isPrimitive();
        boolean secondIsPrimitive = secondType.isPrimitive();
        boolean targetTypeIsPrimitive = targetType.isPrimitive();

        // it is a match or a primitive promotion
        if (targetTypeIsPrimitive && firstIsPrimitive && !secondIsPrimitive) {
            return true;
        }
        if (targetTypeIsPrimitive && !firstIsPrimitive && secondIsPrimitive) {
            return false;
        }

        if (firstType.isPrimitive() || firstType.isArray()) {
            return false;
        }
        if (secondType.isPrimitive() || secondType.isArray()) {
            return false;
        }
        // TODO consider generic parameters?
        return firstType.canBeAssignedTo(secondType) && !secondType.canBeAssignedTo(firstType);
    }

}
