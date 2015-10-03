package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.TypeDefinition;

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

    private String exposedName() {
        if (alias == null) {
            return fieldsPath.getName();
        } else {
            return alias;
        }
    }

    @Override
    public Optional<Node> findAmongImported(String name, SymbolResolver resolver) {
        if (exposedName().equals(name)) {
            String canonicalTypeName = packagePart.qualifiedName() + "." + typeName;
            TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(canonicalTypeName, this, resolver);

            Node importedValue = typeDefinition.getField(fieldsPath, resolver);
            return Optional.of(importedValue);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(packagePart, fieldsPath);
    }
}
