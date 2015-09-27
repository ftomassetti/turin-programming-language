package me.tomassetti.turin.compiler;

import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;

import java.util.Collections;
import java.util.Optional;

public class ExamplesAst {

    public static TurinFile registryAst() {
        // define AST
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("registry");

        turinFile.setNameSpace(namespaceDefinition);

        TurinTypeDefinition person = new TurinTypeDefinition("Person");
        person.setPosition(Position.create(0, 0, 0, 0));
        PropertyDefinition firstNameProperty = new PropertyDefinition("firstName", new ReferenceTypeUsage("String"), Optional.empty(), Optional.empty(), Collections.emptyList());
        person.add(firstNameProperty);
        PropertyDefinition lastNameProperty = new PropertyDefinition("lastName", new ReferenceTypeUsage("String"), Optional.empty(), Optional.empty(), Collections.emptyList());
        person.add(lastNameProperty);

        TurinTypeDefinition address = new TurinTypeDefinition("Address");
        address.setPosition(Position.create(0, 0, 0, 0));
        PropertyDefinition streetProperty = new PropertyDefinition("street", new ReferenceTypeUsage("String"), Optional.empty(), Optional.empty(), Collections.emptyList());
        address.add(streetProperty);
        PropertyDefinition numberProperty = new PropertyDefinition("number", BasicTypeUsage.UINT, Optional.empty(), Optional.empty(), Collections.emptyList());
        address.add(numberProperty);
        PropertyDefinition cityProperty = new PropertyDefinition("city", new ReferenceTypeUsage("String"), Optional.empty(), Optional.empty(), Collections.emptyList());
        address.add(cityProperty);
        PropertyDefinition zipProperty = new PropertyDefinition("zip", BasicTypeUsage.UINT, Optional.empty(), Optional.empty(), Collections.emptyList());
        address.add(zipProperty);

        turinFile.add(person);
        turinFile.add(address);
        return turinFile;
    }

}
