package me.tomassetti.turin.parser.ast.imports;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;

import java.util.Optional;

public class SingleFieldImportDeclaration extends ImportDeclaration {

    public SingleFieldImportDeclaration(QualifiedName packagePart, String typeName, QualifiedName fieldsPath) {
        throw new UnsupportedOperationException();
        // set the parent
    }

    public SingleFieldImportDeclaration(QualifiedName packagePart, String typeName, QualifiedName fieldsPath, String alias) {
        throw new UnsupportedOperationException();
        // set the parent
    }

    @Override
    public Optional<Node> findAmongImported(String name) {
        throw new UnsupportedOperationException();
    }

}
