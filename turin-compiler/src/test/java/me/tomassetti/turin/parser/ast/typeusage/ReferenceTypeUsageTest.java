package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.parser.analysis.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.parser.analysis.resolvers.ResolverRegistry;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ReferenceTypeUsageTest {

    private PropertyReference nameRef;
    private PropertyDefinition ageProperty;
    private TurinFile turinFile;

    @Before
    public void setup() {
        // define AST
        turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsageNode stringType = new ReferenceTypeUsageNode("String");
        BasicTypeUsageNode intType = BasicTypeUsageNode.UINT;

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType, Optional.empty(), Optional.empty(), Collections.emptyList());

        turinFile.add(nameProperty);

        TurinTypeDefinition mangaCharacter = new TurinTypeDefinition("MangaCharacter");
        ageProperty = new PropertyDefinition("age", intType, Optional.empty(), Optional.empty(), Collections.emptyList());
        nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);
    }

    @Test
    public void javaType() {
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        ResolverRegistry.INSTANCE.record(turinFile, resolver);
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
