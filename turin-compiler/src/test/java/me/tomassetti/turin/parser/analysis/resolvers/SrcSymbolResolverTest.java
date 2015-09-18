package me.tomassetti.turin.parser.analysis.resolvers;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.io.IOException;

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

        PropertyDefinition definition = srcSymbolResolver.findDefinition(propertyReference);
        assertEquals("name", definition.getName());
        assertEquals(true, definition.getType().isReferenceTypeUsage());
        assertEquals("java.lang.String", definition.getType().asReferenceTypeUsage().getQualifiedName(symbolResolver));
    }

    @Test(expected = UnsolvedSymbolException.class)
    public void findDefinitionNegativeCase() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/basicManga.to"));
        SrcSymbolResolver srcSymbolResolver = new SrcSymbolResolver(ImmutableList.of(turinFile));
        PropertyReference propertyReference = createMock(PropertyReference.class);
        EasyMock.expect(propertyReference.contextName()).andReturn("not_manga");
        EasyMock.expect(propertyReference.getName()).andReturn("name");

        replayAll();

        srcSymbolResolver.findDefinition(propertyReference);
    }

}
