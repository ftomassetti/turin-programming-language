package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.NoContext;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Optional;

public class TypeImportDeclaration extends ImportDeclaration {

    private QualifiedName qualifiedName;
    private String typeName;
    private String alternativeName;

    @Override
    public String toString() {
        return "TypeImportDeclaration{" +
                "qualifiedName=" + qualifiedName +
                ", typeName='" + typeName + '\'' +
                ", alternativeName='" + alternativeName + '\'' +
                '}';
    }

    public TypeImportDeclaration(QualifiedName qualifiedName, String typeName) {
        this.qualifiedName = qualifiedName;
        this.typeName = typeName;
    }

    public TypeImportDeclaration(QualifiedName qualifiedName, String typeName, String alternativeName) {
        this.qualifiedName = qualifiedName;
        this.typeName = typeName;
        this.alternativeName = alternativeName;
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        findTypeDefinition(resolver);
        if (!typeDefinitionCache.isPresent()) {
            errorCollector.recordSemanticError(getPosition(), "Import not resolved: " + canonicalName());
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    private Optional<TypeDefinition> typeDefinitionCache;

    private String canonicalName() {
        return qualifiedName.qualifiedName() + "." + typeName;
    }

    private void findTypeDefinition(SymbolResolver resolver) {
        if (typeDefinitionCache != null) {
            return;
        }
        typeDefinitionCache = resolver.findTypeDefinitionIn(canonicalName(), NoContext.getInstance(), resolver);
    }

    @Override
    public Optional<Symbol> findAmongImported(String name, SymbolResolver resolver) {
        String targetName = alternativeName == null ? typeName : alternativeName;
        if (name.equals(targetName)) {
            findTypeDefinition(resolver);
            if (typeDefinitionCache.isPresent()) {
                return Optional.of(typeDefinitionCache.get());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(qualifiedName);
    }
}
