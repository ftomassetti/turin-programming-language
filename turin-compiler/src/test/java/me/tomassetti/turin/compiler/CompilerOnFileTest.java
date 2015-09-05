package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.ComposedResolver;
import me.tomassetti.turin.parser.analysis.resolvers.InFileResolver;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.analysis.resolvers.SrcResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class CompilerOnFileTest {

    private Resolver getResolverFor(TurinFile turinFile) {
        return new ComposedResolver(ImmutableList.of(new InFileResolver(), new SrcResolver(ImmutableList.of(turinFile))));
    }

    @Test
    public void compileRanma() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ranma.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(2, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);

        Method getName = mangaCharacterClass.getMethod("getName");
        assertEquals("Ranma", getName.invoke(ranma));

        Method getAge = mangaCharacterClass.getMethod("getAge");
        assertEquals(16, getAge.invoke(ranma));
    }

}
