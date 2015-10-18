package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Optional;

public class AllFieldsImportDeclaration extends ImportDeclaration {

    private QualifiedName packagePart;
    private String typeName;

    public AllFieldsImportDeclaration(QualifiedName packagePart, String typeName) {
        this.packagePart = packagePart;
        this.packagePart.setParent(this);
        this.typeName = typeName;
    }

    private void lookForTypeDefinition(SymbolResolver resolver) {
        if (typeDefinitionCache != null) {
            return;
        }

        typeDefinitionCache = resolver.findTypeDefinitionIn(canonicalTypeName(), this, resolver.getRoot());
    }

    public String canonicalTypeName() {
        return packagePart.qualifiedName() + "." + typeName;
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        lookForTypeDefinition(resolver);
        if (!typeDefinitionCache.isPresent()) {
            errorCollector.recordSemanticError(getPosition(), "Import not resolved: " + canonicalTypeName());
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    private Optional<TypeDefinition> typeDefinitionCache;

    @Override
    public Optional<Symbol> findAmongImported(String name, SymbolResolver resolver) {
        lookForTypeDefinition(resolver);
        if (typeDefinitionCache.isPresent() && typeDefinitionCache.get().hasField(name, true)) {
            return Optional.of(typeDefinitionCache.get().getField(QualifiedName.create(name)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(packagePart);
    }
}
