package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.NoContext;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Optional;

public class TypeImportDeclaration extends ImportDeclaration {

    private QualifiedName qualifiedName;
    private String typeName;
    private String alternativeName;

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
    public Optional<Node> findAmongImported(String name, Resolver resolver) {
        String targetName = alternativeName == null ? typeName : alternativeName;
        if (name.equals(targetName)) {
            Optional<TypeDefinition> res = resolver.findTypeDefinitionIn(qualifiedName.qualifiedName() + "." + typeName, NoContext.getInstance(), resolver);
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
