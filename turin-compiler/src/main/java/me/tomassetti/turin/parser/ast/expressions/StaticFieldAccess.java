package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmFieldDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Optional;

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
    public TypeUsage calcType() {
        TypeDefinition typeDefinition = typeDefinition(symbolResolver());

        TypeUsage fieldType = typeDefinition.getFieldType(field, true);
        return fieldType;
    }

    @Override
    public Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable) {
        TypeDefinition typeDefinition = typeDefinition(symbolResolver());

        if (invokable instanceof Creation) {
            return Optional.of(typeDefinition.getConstructorParams(invokable.getActualParams()));
        } else if (invokable instanceof FunctionCall) {
            return Optional.of(typeDefinition.getMethodParams(field, invokable.getActualParams(), true));
        } else {
            throw new UnsupportedOperationException(invokable.getClass().getCanonicalName());
        }
    }

    @Override
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        TypeDefinition typeDefinition = typeDefinition(resolver);

        return typeDefinition.findMethodFor(field, argsTypes, resolver, staticContext);
    }

    private TypeDefinition typeDefinitionCache;

    private TypeDefinition typeDefinition(SymbolResolver resolver) {
        if (typeDefinitionCache == null) {
            typeDefinitionCache = resolver.getTypeDefinitionIn(subject.qualifiedName(), this);
        }
        return typeDefinitionCache;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(subject);
    }

    public JvmFieldDefinition toJvmField(SymbolResolver resolver) {
        TypeDefinition typeDefinition = typeDefinition(resolver);
        TypeUsage fieldType = typeDefinition.getFieldType(field, true);
        return new JvmFieldDefinition(typeDefinition.getQualifiedName().replaceAll("\\.", "/"), field, fieldType.jvmType().getSignature(), true);
    }
}
