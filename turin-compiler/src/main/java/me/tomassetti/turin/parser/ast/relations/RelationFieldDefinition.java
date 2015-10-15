package me.tomassetti.turin.parser.ast.relations;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.util.StringUtils;
import turin.relations.Relation;

import java.util.List;

public class RelationFieldDefinition extends Node {

    private String name;
    private TypeUsageNode type;

    public boolean isApplicableTo(TypeDefinition typeDefinition, SymbolResolver resolver) {
        if ((type.getParent() instanceof FormalParameter) && type.getParent().getParent() == null){
            throw new UnsupportedOperationException();
        }
        ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(typeDefinition);
        return referenceTypeUsage.canBeAssignedTo(type, resolver);
    }

    public RelationDefinition getRelationDefinition() {
        return (RelationDefinition)getParent();
    }

    public String methodDescriptor(SymbolResolver resolver) {
        if (cardinality == Cardinality.SINGLE) {
            return "(" + otherField().getType().jvmType(resolver).getDescriptor() + ")" + JvmNameUtils.descriptor(Relation.ReferenceSingleEndpoint.class);
        } else if (cardinality == Cardinality.MANY) {
            return "(" + otherField().getType().jvmType(resolver).getDescriptor() + ")" + JvmNameUtils.descriptor(Relation.ReferenceMultipleEndpoint.class);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public enum Cardinality {
        SINGLE,
        MANY
    }

    @Override
    public TypeUsageNode calcType(SymbolResolver resolver) {
        if (cardinality == Cardinality.SINGLE) {
            List<TypeUsageNode> typeParams = getParentOfType(RelationDefinition.class).getTypeParameters();
            ReferenceTypeUsage res = new ReferenceTypeUsage(
                    ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Relation.ReferenceSingleEndpoint.class, typeParams),
                    typeParams);
            return res;
        } else if (cardinality == Cardinality.MANY) {
            List<TypeUsageNode> typeParams = getParentOfType(RelationDefinition.class).getTypeParameters();
            ReferenceTypeUsage res = new ReferenceTypeUsage(
                    ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Relation.ReferenceMultipleEndpoint.class, typeParams),
                    typeParams);
            return res;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getName() {
        return name;
    }

    public TypeUsageNode getType() {
        return type;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    private Cardinality cardinality;

    public RelationFieldDefinition(Cardinality cardinality, String name, TypeUsageNode type) {
        this.cardinality = cardinality;
        this.name = name;
        this.type = type;
        this.type.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(type);
    }

    private RelationFieldDefinition otherField() {
        RelationDefinition relationDefinition = (RelationDefinition)getParent();
        if (relationDefinition.firstField() == this) {
            return relationDefinition.secondField();
        } else if (relationDefinition.secondField() == this) {
            return relationDefinition.firstField();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String methodName() {
        if (otherField().getCardinality() == Cardinality.SINGLE) {
            return getName() + "For" + StringUtils.capitalize(otherField().getName());
        } else if (otherField().getCardinality() == Cardinality.MANY) {
            return getName() + "For" + StringUtils.capitalize(otherField().getName()) + "Element";
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
