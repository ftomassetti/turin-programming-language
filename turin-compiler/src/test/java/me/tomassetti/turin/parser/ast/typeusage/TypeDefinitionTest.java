package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.compiler.ExamplesAst;
import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.ResolverRegistry;
import me.tomassetti.turin.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.typesystem.BasicTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class TypeDefinitionTest {

    private TurinTypeDefinition mangaCharacter;
    private TurinFile turinFile;

    @Before
    public void setup() {
        // define AST
        turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsageNode stringType = new ReferenceTypeUsageNode("String");
        BasicTypeUsageNode intType = new BasicTypeUsageNode("uint");

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType, Optional.empty(), Optional.empty(), Collections.emptyList());

        turinFile.add(nameProperty);

        mangaCharacter = new TurinTypeDefinition("MangaCharacter");
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType, Optional.empty(), Optional.empty(), Collections.emptyList());
        PropertyReference nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);
    }

    @Test
    public void getQualifiedName() {
        assertEquals("manga.MangaCharacter", mangaCharacter.getQualifiedName());
    }

    @Test
    public void getDirectProperties() {
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        ResolverRegistry.INSTANCE.record(turinFile, resolver);
        assertEquals(2, mangaCharacter.getDirectProperties(resolver).size());

        assertEquals("name", mangaCharacter.getDirectProperties(resolver).get(0).getName());
        assertTrue(ReferenceTypeUsage.STRING(resolver).sameType(mangaCharacter.getDirectProperties(resolver).get(0).getTypeUsage()));

        assertEquals("age", mangaCharacter.getDirectProperties(resolver).get(1).getName());
        TypeUsage actualType = mangaCharacter.getDirectProperties(resolver).get(1).getTypeUsage();
        assertTrue(actualType.sameType(BasicTypeUsage.UINT));
    }

    @Test
    public void getDirectPropertiesOnRegistryExample() {
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        TurinFile turinFile = ExamplesAst.registryAst();
        TurinTypeDefinition person = turinFile.getTopTypeDefinition("Person").get();
        assertEquals(2, person.getDirectProperties(resolver).size());
    }

}
