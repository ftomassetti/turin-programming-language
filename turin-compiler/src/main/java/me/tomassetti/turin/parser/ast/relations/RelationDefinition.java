package me.tomassetti.turin.parser.ast.relations;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.NodeTypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import turin.relations.ManyToManyRelation;
import turin.relations.OneToManyRelation;
import turin.relations.OneToOneRelation;

import java.util.List;
import java.util.stream.Collectors;

public class RelationDefinition extends Node {

    public List<RelationFieldDefinition> getFieldsApplicableTo(NodeTypeDefinition typeDefinition, SymbolResolver resolver) {
        return fields.stream().filter((f)->f.isApplicableTo(typeDefinition, resolver)).collect(Collectors.toList());
    }

    List<TypeUsage> getTypeParameters() {
        return ImmutableList.of(
                firstField().getType().copy(),
                secondField().getType().copy()
        );
    }

    public String getGeneratedClassQualifiedName() {
        return contextName() + "." + CLASS_PREFIX + getName();
    }

    public String staticFieldDescriptor() {
        switch (getRelationType()) {
            case MANY_TO_MANY:
                return JvmNameUtils.descriptor(ManyToManyRelation.class);
            case ONE_TO_MANY:
                return JvmNameUtils.descriptor(OneToManyRelation.class);
            case ONE_TO_ONE:
                return JvmNameUtils.descriptor(OneToOneRelation.class);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String relationClassInternalName() {
        switch (getRelationType()) {
            case MANY_TO_MANY:
                return JvmNameUtils.internalName(ManyToManyRelation.class);
            case ONE_TO_MANY:
                return JvmNameUtils.internalName(OneToManyRelation.class);
            case ONE_TO_ONE:
                return JvmNameUtils.internalName(OneToOneRelation.class);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public enum Type {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY
    }

    public static final String CLASS_PREFIX = "Relation_";

    private String name;
    private List<RelationFieldDefinition> fields;

    public String getName() {
        return name;
    }

    public RelationFieldDefinition firstField() {
        return fields.get(0);
    }

    public RelationFieldDefinition secondField() {
        return fields.get(1);
    }

    public RelationFieldDefinition singleField() {
        if (getRelationType() != Type.ONE_TO_MANY) {
            throw new IllegalStateException();
        }
        if (fields.get(0).getCardinality() == RelationFieldDefinition.Cardinality.SINGLE) {
            return fields.get(0);
        } else {
            return fields.get(1);
        }
    }

    public RelationFieldDefinition manyField() {
        if (getRelationType() != Type.ONE_TO_MANY) {
            throw new IllegalStateException();
        }
        if (fields.get(0).getCardinality() == RelationFieldDefinition.Cardinality.MANY) {
            return fields.get(0);
        } else {
            return fields.get(1);
        }
    }

    public List<RelationFieldDefinition> getFields() {
        return fields;
    }

    public RelationDefinition(String name, List<RelationFieldDefinition> fields) {
        this.name = name;
        this.fields = fields;
        this.fields.forEach((f)->f.setParent(RelationDefinition.this));
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        if (fields.size() != 2) {
            errorCollector.recordSemanticError(getPosition(), "Each relation should have exactly 2 fields");
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    public Type getRelationType() {
        int nMany = 0;
        int nSingle = 0;
        for (RelationFieldDefinition field : fields) {
            if (field.getCardinality() == RelationFieldDefinition.Cardinality.SINGLE) {
                nSingle++;
            } else if (field.getCardinality() == RelationFieldDefinition.Cardinality.MANY) {
                nMany++;
            } else {
                throw new UnsupportedOperationException();
            }
        }
        if (nMany == 2 && nSingle == 0) {
            return Type.MANY_TO_MANY;
        } else if (nMany == 0 && nSingle == 2) {
            return Type.ONE_TO_ONE;
        } else if (nMany == 1 && nSingle == 1) {
            return Type.ONE_TO_MANY;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(fields);
    }
}
