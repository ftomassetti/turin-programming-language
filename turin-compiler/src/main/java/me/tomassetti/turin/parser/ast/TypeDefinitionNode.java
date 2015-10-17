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
import me.tomassetti.turin.resolvers.SymbolResolver;
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

}
