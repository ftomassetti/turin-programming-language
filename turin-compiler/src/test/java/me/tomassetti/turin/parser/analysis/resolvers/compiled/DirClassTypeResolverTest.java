package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.compiler.*;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.ComposedSymbolResolver;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.TurinFile;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class DirClassTypeResolverTest extends AbstractCompilerTest {

    // the class files are obtained by compiling formatter3
    private File tmpDir;

    @Before
    public void setup() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/examples/formatter3.to"));
        tmpDir = Files.createTempDirectory("classes").toFile();
        tmpDir.deleteOnExit();
        // generate bytecode
        me.tomassetti.turin.compiler.Compiler instance = new Compiler(getResolverFor(turinFile,  ImmutableList.of("src/test/resources/jars/javaparser-core-2.2.1.jar")), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        for (ClassFileDefinition classFileDefinition : classFileDefinitions) {
            saveClassFile(classFileDefinition, tmpDir.getAbsolutePath());
        }
    }

    @Test
    public void resolveReferenceToCompileTurinFunction() throws IOException {
        DirClassesTypeResolver dirClassesTypeResolver = new DirClassesTypeResolver(tmpDir);
        assertEquals(true, dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.fatalError").isPresent());
        assertEquals(true, dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.format").isPresent());
        assertEquals(true, dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.parse").isPresent());
        assertEquals(false, dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.unexisting").isPresent());
    }

    @Test
    public void returnTypeOfLoadedFunctionIsCorrect() throws IOException {
        SymbolResolver symbolResolver = new ComposedSymbolResolver(ImmutableList.of());
        DirClassesTypeResolver dirClassesTypeResolver = new DirClassesTypeResolver(tmpDir);
        TypeUsageNode returnTypeFatalError = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.fatalError").get().getReturnType();
        TypeUsageNode returnTypeFormat = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.format").get().getReturnType();
        TypeUsageNode returnTypeParse = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.parse").get().getReturnType();
        assertEquals(true, returnTypeFatalError.isVoid());
        assertEquals(true, returnTypeFormat.isReferenceTypeUsage());
        assertEquals(String.class.getCanonicalName(), returnTypeFormat.asReferenceTypeUsage().getQualifiedName(symbolResolver));
        assertEquals(true, returnTypeParse.isReferenceTypeUsage());
        assertEquals(CompilationUnit.class.getCanonicalName(), returnTypeParse.asReferenceTypeUsage().getQualifiedName(symbolResolver));
    }

    @Test
    public void formalParametersOfLoadedFunctionHaveCorrectType() throws IOException {
        SymbolResolver symbolResolver = new ComposedSymbolResolver(ImmutableList.of());
        DirClassesTypeResolver dirClassesTypeResolver = new DirClassesTypeResolver(tmpDir);
        List<? extends FormalParameter> paramsTypeFatalError = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.fatalError").get().getParameters();
        List<? extends FormalParameter> paramsTypeFormat = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.format").get().getParameters();
        List<? extends FormalParameter> paramsTypeParse = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.parse").get().getParameters();

        assertEquals(1, paramsTypeFatalError.size());
        assertEquals(1, paramsTypeFormat.size());
        assertEquals(1, paramsTypeParse.size());

        assertEquals(true, paramsTypeFatalError.get(0).getType().isReferenceTypeUsage());
        assertEquals(String.class.getCanonicalName(), paramsTypeFatalError.get(0).getType().asReferenceTypeUsage().getQualifiedName(symbolResolver));
        assertEquals(true, paramsTypeFormat.get(0).getType().isReferenceTypeUsage());
        assertEquals(CompilationUnit.class.getCanonicalName(), paramsTypeFormat.get(0).getType().asReferenceTypeUsage().getQualifiedName(symbolResolver));
        assertEquals(true, paramsTypeParse.get(0).getType().isReferenceTypeUsage());
        assertEquals(String.class.getCanonicalName(), paramsTypeParse.get(0).getType().asReferenceTypeUsage().getQualifiedName(symbolResolver));
    }

    @Test
    public void formalParametersOfLoadedFunctionHaveCorrectNames() throws IOException {
        DirClassesTypeResolver dirClassesTypeResolver = new DirClassesTypeResolver(tmpDir);
        List<? extends FormalParameter> paramsTypeFatalError = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.fatalError").get().getParameters();
        List<? extends FormalParameter> paramsTypeFormat = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.format").get().getParameters();
        List<? extends FormalParameter> paramsTypeParse = dirClassesTypeResolver.resolveAbsoluteFunctionName("me.tomassetti.javaformatter.parse").get().getParameters();

        assertEquals(1, paramsTypeFatalError.size());
        assertEquals(1, paramsTypeFormat.size());
        assertEquals(1, paramsTypeParse.size());

        assertEquals("msg", paramsTypeFatalError.get(0).getName());
        assertEquals("cu", paramsTypeFormat.get(0).getName());
        assertEquals("path", paramsTypeParse.get(0).getName());
    }

    @Test
    public void referenceToFunctionInClasses() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ref_to_function_in_classes.to"));
        me.tomassetti.turin.compiler.Compiler instance = new Compiler(getResolverFor(turinFile,
                ImmutableList.of("src/test/resources/jars/javaparser-core-2.2.1.jar"),
                ImmutableList.of(tmpDir.getAbsolutePath())), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());
        // it should compile without errors
    }

}
