package me.tomassetti.turin.compiler;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class ImportsTest extends AbstractCompilerTest {

    @Test
    public void importOfTypeWithoutAlias() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        Method method = compileFunction("importOfTypeWithoutAlias", new Class[]{});
        Object res = method.invoke(null);
        assertTrue(res instanceof LinkedList);
    }

    @Test
    public void importOfTypeWithAlias() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        Method method = compileFunction("importOfTypeWithAlias", new Class[]{});
        Object res = method.invoke(null);
        assertTrue(res instanceof LinkedList);
    }

    @Test
    public void importOfTypesInPackage() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        Method method = compileFunction("importOfTypesInPackage", new Class[]{});
        Object res = method.invoke(null);
        assertTrue(res instanceof LinkedList);
    }

    @Test
    public void importOfFieldsInTypeWithoutAlias() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        Method method = compileFunction("importOfFieldsInTypeWithoutAlias", new Class[]{});
        Object res = method.invoke(null);
        assertTrue(res instanceof PrintStream);
        assertEquals(System.out, res);
    }

    @Test
    public void importOfFieldsInTypeWithAlias() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        Method method = compileFunction("importOfFieldsInTypeWithAlias", new Class[]{});
        Object res = method.invoke(null);
        assertTrue(res instanceof PrintStream);
        assertEquals(System.out, res);
    }

    @Test
    public void importOfAllFieldsInType() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        Method method = compileFunction("importOfAllFieldsInType", new Class[]{});
        Object res = method.invoke(null);
        assertTrue(res instanceof PrintStream);
        assertEquals(System.out, res);
    }

}
