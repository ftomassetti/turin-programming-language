package me.tomassetti.turin.compiler.relations;

import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.compiler.AbstractCompilerTest;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;
import turin.relations.OneToManyRelation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RelationsCompilationTest extends AbstractCompilerTest {

    @Test
    public void aClassWithTheCorrectNameIsGeneratedForARelation() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/simple_relation.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());
        assertEquals("relations.Relation_Ast", classDefinitions.get(1).getName());
    }

    @Test
    public void theGeneratedRelationClassHasStaticFieldNeeded() throws IOException, IllegalAccessException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/simple_relation.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());
        Class astClass = new TurinClassLoader().addClass(classDefinitions.get(1));
        saveClassFile(classDefinitions.get(1), "relations");
        assertEquals(1, astClass.getFields().length);
        Field field = astClass.getFields()[0];
        assertEquals("RELATION", field.getName());
        assertTrue(Modifier.isStatic(field.getModifiers()));
        assertTrue(Modifier.isFinal(field.getModifiers()));
        Object value = field.get(null);
        assertNotNull(value);
        assertEquals(OneToManyRelation.class.getCanonicalName(), value.getClass().getCanonicalName());
    }

    @Test
    public void theGeneratedRelationClassHasMethodsForAccessingEndpoints() throws IOException, NoSuchMethodException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/simple_relation.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());
        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class nodeClass = turinClassLoader.addClass(classDefinitions.get(0));
        Class astClass = turinClassLoader.addClass(classDefinitions.get(1));
        assertEquals(2, astClass.getDeclaredMethods().length);
        Method parentForChildrenElement = astClass.getDeclaredMethod("parentForChildrenElement", new Class[]{nodeClass});
        Method childrenForParent = astClass.getDeclaredMethod("childrenForParent", new Class[]{nodeClass});
    }

    @Test
    public void aRelationSubsetIsGeneratedCorrectly() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/relation_subset.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(5, classDefinitions.size());
        assertEquals("relations.Relation_Ast", classDefinitions.get(2).getName());
    }

}
