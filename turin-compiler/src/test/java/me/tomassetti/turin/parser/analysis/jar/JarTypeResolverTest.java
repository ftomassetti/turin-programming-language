package me.tomassetti.turin.parser.analysis.jar;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.resolvers.InFileResolver;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.analysis.resolvers.jar.JarTypeResolver;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class JarTypeResolverTest {

    @Test
    public void canFindExistingClass() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.CompilationUnit");
        assertEquals(true, typeDefinition.isPresent());
    }

    @Test
    public void cannotFindUnexistingClass() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.Foo");
        assertEquals(false, typeDefinition.isPresent());
    }

    @Test
    public void classFoundHasProperQualifiedName() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.CompilationUnit");
        assertEquals("com.github.javaparser.ast.CompilationUnit", typeDefinition.get().getQualifiedName());
    }

    @Test
    public void classFoundHasProperName() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.CompilationUnit");
        assertEquals("CompilationUnit", typeDefinition.get().getName());
    }

    @Test
    public void classFoundHasProperJvmType() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.CompilationUnit");
        assertEquals("Lcom/github/javaparser/ast/CompilationUnit;", typeDefinition.get().jvmType().getSignature());
        assertEquals("Lcom/github/javaparser/ast/CompilationUnit;", typeDefinition.get().jvmType().getDescriptor());
        assertEquals("com/github/javaparser/ast/CompilationUnit", typeDefinition.get().jvmType().getInternalName());
    }

    @Test
    public void methodsCanBeFoundInClassFound() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Resolver resolver = new InFileResolver(jarTypeResolver);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.CompilationUnit");
        JvmMethodDefinition method = typeDefinition.get().findMethodFor("getComments", Collections.emptyList(), resolver, false);
        assertEquals("com/github/javaparser/ast/CompilationUnit", method.getOwnerInternalName());
        assertEquals("()Ljava/util/List;", method.getDescriptor());
        assertEquals("getComments", method.getName());
    }

    @Test
    public void emptyConstructorCanBeFoundInClassFound() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Resolver resolver = new InFileResolver(jarTypeResolver);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.CompilationUnit");
        JvmConstructorDefinition constructor = typeDefinition.get().resolveConstructorCall(resolver, Collections.emptyList());
        assertEquals("com/github/javaparser/ast/CompilationUnit", constructor.getOwnerInternalName());
        assertEquals("()V", constructor.getDescriptor());
        assertEquals("<init>", constructor.getName());
    }

    @Test
    public void testIsInterfaceNegativeCase() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Resolver resolver = new InFileResolver(jarTypeResolver);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.CompilationUnit");
        assertEquals(false, typeDefinition.get().isInterface());
    }

    @Test
    public void testIsInterfacePositiveCase() throws IOException {
        File jarFile = new File("src/test/resources/jars/javaparser-core-2.2.1.jar");
        JarTypeResolver jarTypeResolver = new JarTypeResolver(jarFile);
        Resolver resolver = new InFileResolver(jarTypeResolver);
        Optional<TypeDefinition> typeDefinition = jarTypeResolver.resolveAbsoluteTypeName("com.github.javaparser.ast.DocumentableNode");
        assertEquals(true, typeDefinition.get().isInterface());
    }

}
