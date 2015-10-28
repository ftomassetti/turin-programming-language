package me.tomassetti.turin.compiler.context;

import com.github.javaparser.ast.body.ModifierSet;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.compiler.AbstractCompilerTest;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;
import turin.context.Context;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class ContextCompilationTest extends AbstractCompilerTest {

    @Test
    public void aContextClassIsGeneratedForEachContextDefinition() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/context/context_definition.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());
        assertEquals("a.Context_a", classDefinitions.get(0).getName());
        assertEquals("a.Context_b", classDefinitions.get(1).getName());

        TurinClassLoader classLoader = new TurinClassLoader();
        Class contextA = classLoader.addClass(classDefinitions.get(0));
        Class contextB = classLoader.addClass(classDefinitions.get(1));
        assertEquals(Context.class.getCanonicalName(), contextA.getSuperclass().getCanonicalName());
        assertEquals(Context.class.getCanonicalName(), contextB.getSuperclass().getCanonicalName());
    }

    @Test
    public void aContextClassHasAStaticFieldNamedInstance() throws IOException, NoSuchFieldException, IllegalAccessException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/context/context_definition.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());

        TurinClassLoader classLoader = new TurinClassLoader();
        Class contextA = classLoader.addClass(classDefinitions.get(0));
        Class contextB = classLoader.addClass(classDefinitions.get(1));
        assertEquals(contextA, contextA.getField("INSTANCE").getType());
        assertTrue(ModifierSet.isStatic(contextA.getField("INSTANCE").getModifiers()));
        assertTrue(ModifierSet.isPublic(contextA.getField("INSTANCE").getModifiers()));
        assertTrue(ModifierSet.isFinal(contextA.getField("INSTANCE").getModifiers()));

        assertEquals(contextB, contextB.getField("INSTANCE").getType());
        assertTrue(ModifierSet.isStatic(contextB.getField("INSTANCE").getModifiers()));
        assertTrue(ModifierSet.isPublic(contextB.getField("INSTANCE").getModifiers()));
        assertTrue(ModifierSet.isFinal(contextB.getField("INSTANCE").getModifiers()));

        assertNotNull(contextA.getField("INSTANCE").get(null));
        assertNotNull(contextB.getField("INSTANCE").get(null));
    }

    @Test
    public void contextUsage() throws IOException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/context/context_usage.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(3, classDefinitions.size());

        TurinClassLoader classLoader = new TurinClassLoader();
        Class contextClass = classLoader.addClass(classDefinitions.get(0));
        assertEquals("a.Context_worldName", contextClass.getCanonicalName());
        Class worldNameInCtxClass = classLoader.addClass(classDefinitions.get(1));
        assertEquals("a.Function_worldNameInCtx", worldNameInCtxClass.getCanonicalName());
        saveClassFile(classDefinitions.get(1), "ctx");
        saveClassFile(classDefinitions.get(2), "ctx");
        Class worldNameNoCtxClass = classLoader.addClass(classDefinitions.get(2));
        assertEquals("a.Function_worldNameNoCtx", worldNameNoCtxClass.getCanonicalName());

        assertEquals(Optional.of("Earth"), worldNameInCtxClass.getMethod("invoke", new Class[]{}).invoke(null));
        assertEquals(Optional.empty(), worldNameNoCtxClass.getMethod("invoke", new Class[]{}).invoke(null));
    }

}
