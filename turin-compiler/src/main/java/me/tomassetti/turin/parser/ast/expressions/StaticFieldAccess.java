package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmFieldDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

public class StaticFieldAccess extends Expression {

    private TypeIdentifier subject;
    private String field;

    public StaticFieldAccess(TypeIdentifier subject, String field) {
        this.subject = subject;
        this.subject.setParent(this);
        this.field = field;
    }

    @Override
    public String toString() {
        return "StaticFieldAccess{" +
                "subject=" + subject +
                ", field='" + field + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaticFieldAccess that = (StaticFieldAccess) o;

        if (!field.equals(that.field)) return false;
        if (!subject.equals(that.subject)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + field.hashCode();
        return result;
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        TypeDefinition typeDefinition = typeDefinition(resolver);

        TypeUsage fieldType = typeDefinition.getField(field, true);
        return fieldType;
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        TypeDefinition typeDefinition = typeDefinition(resolver);

        return typeDefinition.findMethodFor(field, argsTypes, resolver, staticContext);
    }

    private TypeDefinition typeDefinition(SymbolResolver resolver) {
        return resolver.getTypeDefinitionIn(subject.qualifiedName(), this, resolver);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(subject);
    }

    public JvmFieldDefinition toJvmField(SymbolResolver resolver) {
        TypeDefinition typeDefinition = typeDefinition(resolver);
        TypeUsage fieldType = typeDefinition.getField(field, true);
        return new JvmFieldDefinition(typeDefinition.getQualifiedName().replaceAll("\\.", "/"), field, fieldType.jvmType(resolver).getSignature(), true);
    }
}
