package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.ComposedReference;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ValueReference;

import java.util.Optional;

public class SingleFieldImportDeclaration extends ImportDeclaration {

    private QualifiedName packagePart;
    private String typeName;
    private QualifiedName fieldsPath;
    private String alias;

    public SingleFieldImportDeclaration(QualifiedName packagePart, String typeName, QualifiedName fieldsPath) {
        this.packagePart = packagePart;
        this.packagePart.setParent(this);
        this.typeName = typeName;
        this.fieldsPath = fieldsPath;
        this.fieldsPath.setParent(this);
    }

    public SingleFieldImportDeclaration(QualifiedName packagePart, String typeName, QualifiedName fieldsPath, String alias) {
        this.packagePart = packagePart;
        this.packagePart.setParent(this);
        this.typeName = typeName;
        this.fieldsPath = fieldsPath;
        this.fieldsPath.setParent(this);
        this.alias = alias;
    }

    @Override
    public Optional<Node> findAmongImported(String name, Resolver resolver) {
        if (alias == null) {
            throw new UnsupportedOperationException();
        } else {
            if (alias.equals(name)) {
                String canonicalTypeName = packagePart.qualifiedName() + "." + typeName;
                TypeDefinition typeDefinition = resolver.findTypeDefinitionIn(canonicalTypeName, this);

                Node importedValue = typeDefinition.getField(fieldsPath, resolver);
                return Optional.of(importedValue);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(packagePart, fieldsPath);
    }
}
