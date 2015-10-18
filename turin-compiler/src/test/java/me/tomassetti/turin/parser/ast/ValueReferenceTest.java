package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.resolvers.ResolverRegistry;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.ValueReference;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.BasicTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class ValueReferenceTest {

    @Test
    public void solveReferenceToLocalVariable() throws IOException {
        String code = "namespace examples\n" +
                "program Example(String[] args) {\n" +
                "    val uint a = 0\n" +
                "    a\n" +
                "}\n";
        InputStream stream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        TurinFile turinFile = new Parser().parse(stream);
        List<ValueReference> valueReferences = turinFile.findAll(ValueReference.class);
        assertEquals(1, valueReferences.size());
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        ResolverRegistry.INSTANCE.record(turinFile, resolver);
        TypeUsage type = valueReferences.get(0).calcType();
        assertTrue(type.sameType(BasicTypeUsage.UINT));
    }

    @Test
    public void solveReferenceToProgramParam() throws IOException {
        String code = "namespace examples\n" +
                "program Example(String[] args) {\n" +
                "    args\n" +
                "}\n";
        InputStream stream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        TurinFile turinFile = new Parser().parse(stream);
        List<ValueReference> valueReferences = turinFile.findAll(ValueReference.class);
        assertEquals(1, valueReferences.size());
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        ResolverRegistry.INSTANCE.record(turinFile, resolver);
        TypeUsage type = valueReferences.get(0).calcType();
        assertTrue(new ArrayTypeUsage(ReferenceTypeUsage.STRING(resolver)).sameType(type));
    }

    @Test
    public void solveReferenceToMethodParam() throws IOException {
        String code = "namespace examples\n" +
                "type Example {\n" +
                "    void foo(int a) = a\n" +
                "}\n";
        InputStream stream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        TurinFile turinFile = new Parser().parse(stream);
        List<ValueReference> valueReferences = turinFile.findAll(ValueReference.class);
        assertEquals(1, valueReferences.size());
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        ResolverRegistry.INSTANCE.record(turinFile, resolver);
        TypeUsage type = valueReferences.get(0).calcType();
        assertTrue(type.isPrimitive());
        assertTrue(type.asPrimitiveTypeUsage().isInt());
    }

}
