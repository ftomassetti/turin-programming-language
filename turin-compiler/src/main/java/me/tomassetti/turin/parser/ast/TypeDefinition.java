package me.tomassetti.turin.parser.ast;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.symbols_definitions.InternalConstructorDefinition;
import me.tomassetti.turin.parser.analysis.symbols_definitions.InternalMethodDefinition;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedConstructorException;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.relations.AccessEndpoint;
import me.tomassetti.turin.parser.ast.relations.RelationDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationFieldDefinition;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsageNode;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Definition of a reference type (a Class, an Interface or an Enum) OR one of the basic types of Turin (like UInt).
 */
public abstract class TypeDefinition extends Node implements Named, Symbol {
    protected String name;

    public TypeDefinition(String name) {
        this.name = name;
    }

    //
    // Naming
    //

    public String getName() {
        return name;
    }

    public abstract String getQualifiedName();

    //
    // Typing
    //

    public JvmType jvmType() {
        return new JvmType(JvmNameUtils.canonicalToDescriptor(getQualifiedName()));
    }

    //
    // Constructors
    //

    public abstract JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams);

    public final boolean hasManyConstructors(SymbolResolver resolver) {
        return getConstructors(resolver).size() > 1;
    }

    public final List<? extends FormalParameter> getConstructorParams(List<ActualParam> actualParams, SymbolResolver resolver) {
        return getConstructor(actualParams, resolver).getFormalParameters();
    }

    public final Optional<JvmConstructorDefinition> findConstructorDefinition(List<ActualParam> actualParams, SymbolResolver resolver) {
        Optional<InternalConstructorDefinition> res = findConstructor(actualParams, resolver);
        if (res.isPresent()) {
            return Optional.of(res.get().getJvmConstructorDefinition());
        } else {
            return Optional.empty();
        }
    }

    public abstract Optional<InternalConstructorDefinition> findConstructor(List<ActualParam> actualParams, SymbolResolver resolver);

    public final InternalConstructorDefinition getConstructor(List<ActualParam> actualParams, SymbolResolver resolver) {
        Optional<InternalConstructorDefinition> constructor = findConstructor(actualParams, resolver);
        if (constructor.isPresent()) {
            return constructor.get();
        } else {
            throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
        }
    }

    public abstract List<InternalConstructorDefinition> getConstructors(SymbolResolver resolver);

    //
    // Methods
    //

    public abstract JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext);

    public abstract boolean isMethodOverloaded(String methodName, SymbolResolver resolver);

    public abstract Optional<InternalMethodDefinition> findMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    public final TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return getMethod(methodName, actualParams, resolver, staticContext).getReturnType();
    }

    public final List<? extends FormalParameter> getMethodParams(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return getMethod(methodName, actualParams, resolver, staticContext).getFormalParameters();
    }

    public final boolean hasMethodFor(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return findMethod(methodName, actualParams, resolver, staticContext).isPresent();
    }

    public final InternalMethodDefinition getMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        Optional<InternalMethodDefinition> method = findMethod(methodName, actualParams, resolver, staticContext);
        if (method.isPresent()) {
            return method.get();
        } else {
            throw new UnsolvedMethodException(getQualifiedName(), methodName, actualParams);
        }
    }

    //
    // Fields
    //

    public abstract TypeUsage getFieldType(String fieldName, boolean staticContext, SymbolResolver resolver);

    public Node getFieldOnInstance(String fieldName, Node instance, final SymbolResolver resolver) {
        for (RelationDefinition relationDefinition : getVisibleRelations(resolver)) {
            for (RelationFieldDefinition field : relationDefinition.getFieldsApplicableTo(this, resolver)) {
                if (field.getName().equals(fieldName)) {
                    return new AccessEndpoint(instance, field);
                }
            }
        }
        throw new UnsolvedSymbolException(this, fieldName);
    }

    private List<RelationDefinition> getVisibleRelations(SymbolResolver resolver) {
        List<RelationDefinition> relations = new LinkedList<>();
        collectVisibleRelations(this, relations, resolver);
        return relations;

    }

    private void collectVisibleRelations(Node context, List<RelationDefinition> relations, SymbolResolver resolver) {
        // TODO consider relations imported
        for (Node child : context.getChildren()) {
            if (child instanceof RelationDefinition) {
                relations.add((RelationDefinition)child);
            }
        }
        if (context.getParent() != null) {
            collectVisibleRelations(context.getParent(), relations, resolver);
        }
    }

    public abstract boolean hasField(String name, boolean staticContext);

    public final boolean hasField(QualifiedName fieldName, boolean staticContext, SymbolResolver resolver) {
        if (!fieldName.isSimpleName()) {
            String firstName = fieldName.firstSegment();
            if (!hasField(firstName, staticContext)) {
                return false;
            }
            Node field = getField(firstName, resolver);
            TypeUsage typeUsage = field.calcType(resolver);
            if (typeUsage.isReferenceTypeUsage()) {
                TypeDefinition typeOfFirstField = typeUsage.asReferenceTypeUsage().getTypeDefinition(resolver);
                return typeOfFirstField.hasField(fieldName.rest(), true, resolver) || typeOfFirstField.hasField(fieldName.rest(), false, resolver);
            } else {
                return false;
            }
        }
        return hasField(fieldName.getName(), staticContext);
    }

    public abstract boolean canFieldBeAssigned(String field, SymbolResolver resolver);

    //
    // Hierarchy
    //

    public abstract List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver);

    public abstract boolean isInterface();

    public abstract boolean isClass();

    public abstract TypeDefinition getSuperclass(SymbolResolver resolver);

    public abstract <T extends TypeUsage> Map<String, TypeUsage> associatedTypeParametersToName(SymbolResolver resolver, List<T> typeParams);
}
