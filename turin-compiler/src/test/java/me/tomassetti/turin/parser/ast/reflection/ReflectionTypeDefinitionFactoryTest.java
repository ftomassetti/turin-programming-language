package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.resolvers.jdk.ReflectionTypeDefinitionFactory;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReflectionTypeDefinitionFactoryTest {

    @Test
    public void calcSignatureForBoolean(){
        assertEquals("Z", ReflectionTypeDefinitionFactory.calcSignature(boolean.class));
    }

    @Test
    public void calcSignatureForByte(){
        assertEquals("B", ReflectionTypeDefinitionFactory.calcSignature(byte.class));
    }

    @Test
    public void calcSignatureForShort(){
        assertEquals("S", ReflectionTypeDefinitionFactory.calcSignature(short.class));
    }

    @Test
    public void calcSignatureForInt(){
        assertEquals("I", ReflectionTypeDefinitionFactory.calcSignature(int.class));
    }

    @Test
    public void calcSignatureForLong(){
        assertEquals("J", ReflectionTypeDefinitionFactory.calcSignature(long.class));
    }

    @Test
    public void calcSignatureForChar(){
        assertEquals("C", ReflectionTypeDefinitionFactory.calcSignature(char.class));
    }

    @Test
    public void calcSignatureForFloat(){
        assertEquals("F", ReflectionTypeDefinitionFactory.calcSignature(float.class));
    }

    @Test
    public void calcSignatureForDouble(){
        assertEquals("D", ReflectionTypeDefinitionFactory.calcSignature(double.class));
    }

    @Test
    public void calcSignatureForVoid(){
        assertEquals("V", ReflectionTypeDefinitionFactory.calcSignature(void.class));
    }

    @Test
    public void calcSignatureForString(){
        assertEquals("Ljava/lang/String;", ReflectionTypeDefinitionFactory.calcSignature(String.class));
    }

    @Test
    public void calcSignatureForArrayOfString(){
        assertEquals("[Ljava/lang/String;", ReflectionTypeDefinitionFactory.calcSignature(String[].class));
    }

    @Test
    public void calcSignatureForArrayOfArrayOfString(){
        assertEquals("[[Ljava/lang/String;", ReflectionTypeDefinitionFactory.calcSignature(String[][].class));
    }

}
