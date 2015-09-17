package me.tomassetti.turin.compiler;

import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class CompilerJarReferenceTest extends AbstractCompilerTest {

    @Test
    public void compileFunctionInstantiatingTypeFromJar() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method invoke = compileFunction("use_jar_constructor", new Class[]{String.class}, ImmutableList.of("src/test/resources/jars/javaparser-core-2.2.1.jar"));
        Comment comment = (Comment)invoke.invoke(null, "qwerty");
        assertEquals("qwerty", comment.getContent());
    }

    @Test
    public void compileFunctionUsingMethodTypeFromJar() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method invoke = compileFunction("use_jar_method", new Class[]{Comment.class}, ImmutableList.of("src/test/resources/jars/javaparser-core-2.2.1.jar"));
        Comment comment = new LineComment("qwerty");
        assertEquals("qwerty", invoke.invoke(null, comment));
    }

    @Test
    public void compileFunctionUsingContructorAndMethodTypeFromJar() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method invoke = compileFunction("use_jar", new Class[]{String.class}, ImmutableList.of("src/test/resources/jars/javaparser-core-2.2.1.jar"));
        assertEquals("qwerty", invoke.invoke(null, "qwerty"));
    }
}
