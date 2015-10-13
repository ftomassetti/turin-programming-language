package me.tomassetti.turin.compiler.relations;

import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.compiler.AbstractCompilerTest;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RelationsCompilationTest extends AbstractCompilerTest {

    @Test
    public void aClassWithTheCorrectNameIsGeneratedForARelation() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/simple_relation.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(3, classDefinitions.size());
        assertEquals("relations.Relation_Ast", classDefinitions.get(2).getName());
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
