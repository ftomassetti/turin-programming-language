package me.tomassetti.turin.parser.ast;

import org.junit.Test;
import static org.junit.Assert.*;

public class TurinTypeDefinitionTest {

    @Test
    public void getQualifiedName() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        turinFile.add(typeDefinition);
        assertEquals("me.tomassetti.MyType", typeDefinition.getQualifiedName());
    }

}
