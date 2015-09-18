package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.analysis.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionTypeDefinitionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReferenceTypeUsageTest {

    private PropertyReference nameRef;
    private PropertyDefinition ageProperty;

    @Before
    public void setup() {
        // define AST
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        BasicTypeUsage intType = BasicTypeUsage.UINT;

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType);

        turinFile.add(nameProperty);

        TurinTypeDefinition mangaCharacter = new TurinTypeDefinition("MangaCharacter");
        ageProperty = new PropertyDefinition("age", intType);
        nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);
    }

    @Test
    public void javaType() {
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        assertEquals("Ljava/lang/String;", nameRef.getType(resolver).jvmType(resolver).getSignature());
        assertEquals("I", ageProperty.getType().jvmType(resolver).getSignature());
    }

    @Test
    public void isInterfaceNegativeCase() {
        TypeDefinition typeDefinition = ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(String.class);
        ReferenceTypeUsage typeUsage = new ReferenceTypeUsage(typeDefinition);
        assertEquals(false, typeUsage.isInterface(new InFileSymbolResolver(JdkTypeResolver.getInstance())));
    }

    @Test
    public void isInterfacePositiveCase() {
        TypeDefinition typeDefinition = ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(List.class);
        ReferenceTypeUsage typeUsage = new ReferenceTypeUsage(typeDefinition);
        assertEquals(true, typeUsage.isInterface(new InFileSymbolResolver(JdkTypeResolver.getInstance())));
    }

}
