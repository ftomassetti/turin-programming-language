package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.Position;
import me.tomassetti.turin.parser.ast.TurinFile;
import me.tomassetti.turin.parser.ast.statements.ThrowStatement;
import org.easymock.EasyMock;
import org.easymock.internal.matchers.Any;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class DuplicateNamesCompilationTest extends AbstractCompilerTest {

    @Test
    public void overloadedFunctionsAreNotAllowed() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/duplicate_functions.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(EasyMock.eq(Position.create(6, 0, 7, 2)), EasyMock.anyString());
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }

    @Test
    public void duplicateProgramsAreNotAllowed() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/duplicate_programs.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(EasyMock.eq(Position.create(6, 0, 7, 2)), EasyMock.anyString());
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }

    @Test
    public void duplicateTypesAreNotAllowed() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/duplicate_types.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(EasyMock.eq(Position.create(6, 0, 7, 2)), EasyMock.anyString());
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }

}
