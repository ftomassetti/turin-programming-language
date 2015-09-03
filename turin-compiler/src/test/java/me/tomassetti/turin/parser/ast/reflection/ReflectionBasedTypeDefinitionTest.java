package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.parser.analysis.InFileResolver;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ReflectionBasedTypeDefinitionTest {

    @Test
    public void getAllAncestorsOfString(){
        Resolver resolver = new InFileResolver();
        TypeDefinition typeDefinition = ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(String.class);
        List<TypeDefinition> ancestors = typeDefinition.getAllAncestors(resolver);
        assertEquals(4, ancestors.size());
        Set<String> names = ancestors.stream().map((a)->a.getQualifiedName()).collect(Collectors.toSet());
        assertTrue(names.contains(Object.class.getCanonicalName()));
        assertTrue(names.contains(Serializable.class.getCanonicalName()));
        assertTrue(names.contains(CharSequence.class.getCanonicalName()));
        assertTrue(names.contains(Comparable.class.getCanonicalName()));
        // TODO verify the parameter of Comparable is String
    }

    @Test
    public void getAllAncestorsOfObject(){
        Resolver resolver = new InFileResolver();
        TypeDefinition typeDefinition = ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Object.class);
        List<TypeDefinition> ancestors = typeDefinition.getAllAncestors(resolver);
        assertEquals(0, ancestors.size());
    }

    @Test
    public void getAllAncestorsOfSerializable(){
        Resolver resolver = new InFileResolver();
        TypeDefinition typeDefinition = ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Object.class);
        List<TypeDefinition> ancestors = typeDefinition.getAllAncestors(resolver);
        assertEquals(0, ancestors.size());
    }

}
