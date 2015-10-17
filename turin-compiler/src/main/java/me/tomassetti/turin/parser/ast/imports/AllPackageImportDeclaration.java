package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Optional;

public class AllPackageImportDeclaration extends ImportDeclaration {

    private QualifiedName qualifiedName;

    public AllPackageImportDeclaration(QualifiedName qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        if (!resolver.existPackage(qualifiedName.qualifiedName())) {
            errorCollector.recordSemanticError(getPosition(), "Import not resolved: " + qualifiedName.qualifiedName());
            return false;
        }

        return super.specificValidate(resolver, errorCollector);
    }

    @Override
    public Optional<Symbol> findAmongImported(String name, SymbolResolver resolver) {
        // TODO correct the context passed
        if (JvmNameUtils.isSimpleName(name)) {
            Optional<TypeDefinition> res = resolver.findTypeDefinitionIn(qualifiedName.qualifiedName() + "." + name, this, resolver);
            if (res.isPresent()) {
                return Optional.of(res.get());
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
