package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.JdkTypeResolver;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayTypeUsageTest {

    private ArrayTypeUsage arrayOfBoolean;
    private ArrayTypeUsage arrayOfString;
    private ArrayTypeUsage arrayOfArrayOfString;

    @Before
    public void setup() {
        PrimitiveTypeUsage primitiveTypeUsage = PrimitiveTypeUsage.BOOLEAN;
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        arrayOfBoolean = new ArrayTypeUsage(primitiveTypeUsage);
        arrayOfString = new ArrayTypeUsage(ReferenceTypeUsage.STRING(resolver));
        arrayOfArrayOfString = new ArrayTypeUsage(arrayOfString);
    }

    @Test
    public void testIsArray() {
        assertEquals(true, arrayOfBoolean.isArray());
        assertEquals(true, arrayOfString.isArray());
        assertEquals(true, arrayOfArrayOfString.isArray());
    }

    @Test
    public void testIsPrimitive() {
        assertEquals(false, arrayOfBoolean.isPrimitive());
        assertEquals(false, arrayOfString.isPrimitive());
        assertEquals(false, arrayOfArrayOfString.isPrimitive());
    }

    @Test
    public void testIsReferenceTypeUsage() {
        assertEquals(false, arrayOfBoolean.isReferenceTypeUsage());
        assertEquals(false, arrayOfString.isReferenceTypeUsage());
        assertEquals(false, arrayOfArrayOfString.isReferenceTypeUsage());
    }

    @Test
    public void testIsVoid() {
        assertEquals(false, arrayOfBoolean.isVoid());
        assertEquals(false, arrayOfString.isVoid());
        assertEquals(false, arrayOfArrayOfString.isVoid());
    }

    @Test
    public void testAsReferenceTypeUsage() {
        int exceptions = 0;
        try {
            arrayOfBoolean.asReferenceTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            arrayOfString.asReferenceTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            arrayOfArrayOfString.asReferenceTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        assertEquals(3, exceptions);
    }

    @Test
    public void testAsArrayTypeUsage() {
        assertEquals(arrayOfBoolean, arrayOfBoolean.asArrayTypeUsage());
        assertEquals(arrayOfString, arrayOfString.asArrayTypeUsage());
        assertEquals(arrayOfArrayOfString, arrayOfArrayOfString.asArrayTypeUsage());
    }

    @Test
    public void testAsPrimitiveTypeUsage() {
        int exceptions = 0;
        try {
            arrayOfBoolean.asPrimitiveTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            arrayOfString.asPrimitiveTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            arrayOfArrayOfString.asPrimitiveTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        assertEquals(3, exceptions);
    }

    @Test
    public void testIsReference() {
        assertEquals(true, arrayOfBoolean.isReference());
        assertEquals(true, arrayOfString.isReference());
        assertEquals(true, arrayOfArrayOfString.isReference());
    }

    @Test
    public void testJvmType() {
        assertEquals("[Z", arrayOfBoolean.jvmType().getSignature());
        assertEquals("[Ljava/lang/String;", arrayOfString.jvmType().getSignature());
        assertEquals("[[Ljava/lang/String;", arrayOfArrayOfString.jvmType().getSignature());
    }

}
