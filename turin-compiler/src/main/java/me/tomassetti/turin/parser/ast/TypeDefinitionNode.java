package me.tomassetti.turin.parser.ast;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedSymbolException;
import me.tomassetti.turin.definitions.InternalConstructorDefinition;
import me.tomassetti.turin.definitions.InternalMethodDefinition;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedConstructorException;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.relations.AccessEndpoint;
import me.tomassetti.turin.parser.ast.relations.RelationDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationFieldDefinition;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Definition of a reference type (a Class, an Interface or an Enum) OR one of the basic types of Turin (like UInt).
 */
public abstract class TypeDefinitionNode extends Node implements me.tomassetti.turin.definitions.TypeDefinition {
    protected String name;

    public TypeDefinitionNode(String name) {
        this.name = name;
    }

    //
    // Naming
    //

    @Override
    public String getName() {
        return name;
    }

    //
    // Typing
    //

    @Override
    public JvmType jvmType() {
        return new JvmType(JvmNameUtils.canonicalToDescriptor(getQualifiedName()));
    }

    //
    // Constructors
    //

    @Override
    public final boolean hasManyConstructors(SymbolResolver resolver) {
        return getConstructors(resolver).size() > 1;
    }

    @Override
    public final List<? extends FormalParameter> getConstructorParams(List<ActualParam> actualParams, SymbolResolver resolver) {
        return getConstructor(actualParams, resolver).getFormalParameters();
    }

    @Override
    public final Optional<JvmConstructorDefinition> findConstructorDefinition(List<ActualParam> actualParams, SymbolResolver resolver) {
        Optional<InternalConstructorDefinition> res = findConstructor(actualParams, resolver);
        if (res.isPresent()) {
            return Optional.of(res.get().getJvmConstructorDefinition());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public final InternalConstructorDefinition getConstructor(List<ActualParam> actualParams, SymbolResolver resolver) {
        Optional<InternalConstructorDefinition> constructor = findConstructor(actualParams, resolver);
        if (constructor.isPresent()) {
            return constructor.get();
        } else {
            throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
        }
    }

    //
    // Methods
    //

    @Override
    public final TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return getMethod(methodName, actualParams, resolver, staticContext).getReturnType();
    }

    @Override
    public final List<? extends FormalParameter> getMethodParams(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return getMethod(methodName, actualParams, resolver, staticContext).getFormalParameters();
    }

    @Override
    public final boolean hasMethodFor(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return findMethod(methodName, actualParams, resolver, staticContext).isPresent();
    }

    @Override
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

    @Override
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

    @Override
    public final boolean hasField(QualifiedName fieldName, boolean staticContext, SymbolResolver resolver) {
        if (!fieldName.isSimpleName()) {
            String firstName = fieldName.firstSegment();
            if (!hasField(firstName, staticContext)) {
                return false;
            }
            Symbol field = getField(firstName, resolver);
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

    //
    // Hierarchy
    //

}
