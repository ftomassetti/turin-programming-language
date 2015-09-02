package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.JvmFieldDefinition;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.TypeUsage;

public class StaticFieldAccess extends Expression {

    private TypeIdentifier subject;
    private String field;

    public StaticFieldAccess(TypeIdentifier subject, String field) {
        this.subject = subject;
        this.field = field;
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        TypeDefinition typeDefinition = typeDefinition(resolver);
        TypeUsage fieldType = typeDefinition.getField(field, true);
        return fieldType;
    }

    private TypeDefinition typeDefinition(Resolver resolver) {
        return resolver.findTypeDefinitionIn(subject.qualifiedName(), this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(subject);
    }

    public JvmFieldDefinition toJvmField(Resolver resolver) {
        TypeDefinition typeDefinition = typeDefinition(resolver);
        TypeUsage fieldType = typeDefinition.getField(field, true);
        return new JvmFieldDefinition(typeDefinition.getQualifiedName().replaceAll("\\.", "/"), field, fieldType.jvmType(resolver).getSignature(), true);
    }
}
