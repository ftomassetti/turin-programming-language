package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Optional;

public class AllFieldsImportDeclaration extends ImportDeclaration {

    private QualifiedName packagePart;
    private String typeName;

    public AllFieldsImportDeclaration(QualifiedName packagePart, String typeName) {
        this.packagePart = packagePart;
        this.packagePart.setParent(this);
        this.typeName = typeName;
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        String canonicalTypeName = packagePart.qualifiedName() + "." + typeName;
        Optional<TypeDefinition> result = resolver.findTypeDefinitionIn(typeName, this, resolver.getRoot());
        if (result.isPresent()) {
            typeDefinitionCache = result.get();
        } else {
            errorCollector.recordSemanticError(getPosition(), "Import not resolver: " + canonicalTypeName);
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    private TypeDefinition typeDefinitionCache;

    @Override
    public Optional<Node> findAmongImported(String name, SymbolResolver resolver) {
        if (typeDefinitionCache == null) {
            String canonicalTypeName = packagePart.qualifiedName() + "." + typeName;
            typeDefinitionCache = resolver.getTypeDefinitionIn(canonicalTypeName, this, resolver);
        }
        if (typeDefinitionCache.hasField(name, true)) {
            return Optional.of(typeDefinitionCache.getField(QualifiedName.create(name), resolver));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(packagePart);
    }
}
