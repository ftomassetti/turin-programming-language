package me.tomassetti.turin.parser.analysis.resolvers;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.TypeUsage;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class SrcSymbolResolverTest extends EasyMockSupport {

    @Test
    public void findDefinitionPositiveCase() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/basicManga.to"));
        SrcSymbolResolver srcSymbolResolver = new SrcSymbolResolver(ImmutableList.of(turinFile));
        PropertyReference propertyReference = createMock(PropertyReference.class);
        EasyMock.expect(propertyReference.contextName()).andReturn("manga");
        EasyMock.expect(propertyReference.getName()).andReturn("name");

        replayAll();

        SymbolResolver symbolResolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        ResolverRegistry.INSTANCE.record(turinFile, symbolResolver);

        Optional<PropertyDefinition> optionalDefinition = srcSymbolResolver.findDefinition(propertyReference);
        assertEquals(true, optionalDefinition.isPresent());
        PropertyDefinition definition = optionalDefinition.get();
        assertEquals("name", definition.getName());
        assertEquals(true, definition.getType().isReferenceTypeUsage());
        assertEquals("java.lang.String", definition.getType().asReferenceTypeUsage().getQualifiedName(symbolResolver));
    }

    @Test
    public void findDefinitionNegativeCase() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/basicManga.to"));
        SrcSymbolResolver srcSymbolResolver = new SrcSymbolResolver(ImmutableList.of(turinFile));
        PropertyReference propertyReference = createMock(PropertyReference.class);
        EasyMock.expect(propertyReference.contextName()).andReturn("not_manga");
        EasyMock.expect(propertyReference.getName()).andReturn("name");

        replayAll();

        Optional<PropertyDefinition> optionalDefinition = srcSymbolResolver.findDefinition(propertyReference);
        assertEquals(false, optionalDefinition.isPresent());
    }

    @Test
    public void findTypeDefinitionInPositiveCase() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/basicManga.to"));
        SrcSymbolResolver srcSymbolResolver = new SrcSymbolResolver(ImmutableList.of(turinFile));

        SymbolResolver symbolResolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());

        Optional<TypeDefinition> typeDefinition = srcSymbolResolver.findTypeDefinitionIn("manga.MangaCharacter", NoContext.getInstance(), symbolResolver);
        assertEquals(true, typeDefinition.isPresent());
        assertEquals("manga.MangaCharacter", typeDefinition.get().getQualifiedName());
    }

    @Test
    public void findTypeDefinitionInNegativeCase() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/basicManga.to"));
        SrcSymbolResolver srcSymbolResolver = new SrcSymbolResolver(ImmutableList.of(turinFile));

        SymbolResolver symbolResolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());

        Optional<TypeDefinition> typeDefinition = srcSymbolResolver.findTypeDefinitionIn("not_manga.MangaCharacter", NoContext.getInstance(), symbolResolver);
        assertEquals(false, typeDefinition.isPresent());
    }

    @Test
    public void findTypeUsageInPositiveCase() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/basicManga.to"));
        SrcSymbolResolver srcSymbolResolver = new SrcSymbolResolver(ImmutableList.of(turinFile));

        SymbolResolver symbolResolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());

        Optional<TypeUsage> typeDefinition = srcSymbolResolver.findTypeUsageIn("manga.MangaCharacter", NoContext.getInstance(), symbolResolver);
        assertEquals(true, typeDefinition.isPresent());
        assertEquals(true, typeDefinition.get().isReferenceTypeUsage());
        assertEquals("manga.MangaCharacter", typeDefinition.get().asReferenceTypeUsage().getQualifiedName(srcSymbolResolver));
    }

    @Test
    public void findTypeUsageInNegativeCase() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/basicManga.to"));
        SrcSymbolResolver srcSymbolResolver = new SrcSymbolResolver(ImmutableList.of(turinFile));

        SymbolResolver symbolResolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());

        Optional<TypeUsage> typeDefinition = srcSymbolResolver.findTypeUsageIn("not_manga.MangaCharacter", NoContext.getInstance(), symbolResolver);
        assertEquals(false, typeDefinition.isPresent());
    }

}
