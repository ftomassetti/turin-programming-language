package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.jvm.JvmTypeCategory;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A TypeUsage is the concrete usage of a type int the code.
 * For example it can be a type definition with generic type parameter specified.
 */
public abstract class TypeUsageNode extends Node implements TypeUsage {

    public TypeUsage typeUsage() {
        return this;
    }

    public static TypeUsageNode wrap(TypeUsage typeUsage) {
        return new TypeUsageWrapperNode(typeUsage) {
            @Override
            public TypeUsageNode copy() {
                return this;
            }

            @Override
            public Iterable<Node> getChildren() {
                return Collections.emptyList();
            }

            @Override
            public String toString() {
                return "TypeUsageWrapperNode{"+typeUsage+"}";
            }
        };
    }

    public static class TypeVariableData {
        private TypeVariableUsage.GenericDeclaration genericDeclaration;
        private List<? extends TypeUsage> bounds;

        public TypeVariableData(TypeVariableUsage.GenericDeclaration genericDeclaration, List<? extends TypeUsage> bounds) {
            this.genericDeclaration = genericDeclaration;
            this.bounds = bounds;
        }

        public TypeVariableUsage.GenericDeclaration getGenericDeclaration() {
            return genericDeclaration;
        }

        public List<? extends TypeUsage> getBounds() {
            return bounds;
        }
    }

    public static TypeUsage fromJvmType(JvmType jvmType, SymbolResolver resolver, Map<String, TypeVariableData> visibleGenericTypes) {
        Optional<PrimitiveTypeUsage> primitive = PrimitiveTypeUsage.findByJvmType(jvmType);
        if (primitive.isPresent()) {
            return primitive.get();
        }
        String signature = jvmType.getSignature();
        if (signature.startsWith("[")) {
            JvmType componentType = new JvmType(signature.substring(1));
            return new ArrayTypeUsage(fromJvmType(componentType, resolver, visibleGenericTypes));
        } else if (signature.startsWith("L") && signature.endsWith(";")) {
            String typeName = signature.substring(1, signature.length() - 1);
            typeName = typeName.replaceAll("/", ".");
            Optional<TypeDefinition> typeDefinition = resolver.findTypeDefinitionIn(typeName, null, resolver);
            if (!typeDefinition.isPresent()) {
                throw new RuntimeException("Unable to find definition of type " + typeName + " using " + resolver);
            }
            return new ReferenceTypeUsage(typeDefinition.get());
        } else if (signature.equals("V")) {
            return new VoidTypeUsage();
        } else {
            for (String typeVariableName : visibleGenericTypes.keySet()) {
                if (typeVariableName.equals(signature)) {
                    TypeVariableData typeVariableData = visibleGenericTypes.get(typeVariableName);
                    return new TypeVariableUsage(typeVariableData.genericDeclaration, typeVariableName, typeVariableData.getBounds());
                }
            }
            throw new UnsupportedOperationException("Signature="+signature+", type="+jvmType.getClass());
        }
    }

    public final JvmTypeCategory toJvmTypeCategory() {
        return this.jvmType().typeCategory();
    }

    @Override
    public boolean isReferenceTypeUsage() {
        return false;
    }

    @Override
    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public me.tomassetti.turin.typesystem.ArrayTypeUsage asArrayTypeUsage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public PrimitiveTypeUsage asPrimitiveTypeUsage() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    public abstract TypeUsageNode copy();

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
