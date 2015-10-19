package me.tomassetti.turin.typesystem;

import com.google.common.collect.ImmutableMap;
import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.symbols.Symbol;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class ArrayTypeUsageTest {

    private ArrayTypeUsage arrayOfBoolean;
    private ArrayTypeUsage arrayOfString;
    private ArrayTypeUsage arrayOfArrayOfString;
    private ReferenceTypeUsage string;
    private ReferenceTypeUsage object;

    @Before
    public void setup() {
        PrimitiveTypeUsage primitiveTypeUsage = PrimitiveTypeUsage.BOOLEAN;
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        string = ReferenceTypeUsage.STRING(resolver);
        object = ReferenceTypeUsage.OBJECT(resolver);
        arrayOfBoolean = new ArrayTypeUsage(primitiveTypeUsage);
        arrayOfString = new ArrayTypeUsage(string);
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
    public void testIsInvokable() {
        assertEquals(false, arrayOfBoolean.isInvokable());
        assertEquals(false, arrayOfString.isInvokable());
        assertEquals(false, arrayOfArrayOfString.isInvokable());
    }

    @Test
    public void testAsInvokable() {
        int exceptions = 0;
        try {
            arrayOfBoolean.asInvokable();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            arrayOfString.asInvokable();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            arrayOfArrayOfString.asInvokable();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        assertEquals(3, exceptions);
    }

    @Test
    public void testJvmType() {
        assertEquals("[Z", arrayOfBoolean.jvmType().getSignature());
        assertEquals("[Ljava/lang/String;", arrayOfString.jvmType().getSignature());
        assertEquals("[[Ljava/lang/String;", arrayOfArrayOfString.jvmType().getSignature());
    }

    @Test
    public void testHasInstanceField() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        assertEquals(true, arrayOfBoolean.hasInstanceField("length", instance));
        assertEquals(true, arrayOfString.hasInstanceField("length", instance));
        assertEquals(true, arrayOfArrayOfString.hasInstanceField("length", instance));
        assertEquals(false, arrayOfBoolean.hasInstanceField("foo", instance));
        assertEquals(false, arrayOfString.hasInstanceField("foo", instance));
        assertEquals(false, arrayOfArrayOfString.hasInstanceField("foo", instance));
        EasyMock.verify(instance);
    }

    @Test
    public void testGetInstanceFieldLength() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        assertTrue(arrayOfBoolean.getInstanceField("length", instance).calcType().sameType(PrimitiveTypeUsage.INT));
        assertTrue(arrayOfString.getInstanceField("length", instance).calcType().sameType(PrimitiveTypeUsage.INT));
        assertTrue(arrayOfArrayOfString.getInstanceField("length", instance).calcType().sameType(PrimitiveTypeUsage.INT));
        EasyMock.verify(instance);
    }

    @Test
    public void testGetInstanceFieldUnexisting() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        int exceptions = 0;
        try {
            arrayOfBoolean.getInstanceField("foo", instance);
        } catch (IllegalArgumentException uoe) {
            exceptions++;
        }
        try {
            arrayOfString.getInstanceField("foo", instance);
        } catch (IllegalArgumentException uoe) {
            exceptions++;
        }
        try {
            arrayOfArrayOfString.getInstanceField("foo", instance);
        } catch (IllegalArgumentException uoe) {
            exceptions++;
        }
        assertEquals(3, exceptions);
        EasyMock.verify(instance);
    }

    @Test
    public void testGetMethod() {
        assertFalse(arrayOfBoolean.getMethod("foo", true).isPresent());
        assertFalse(arrayOfBoolean.getMethod("foo", false).isPresent());
        assertFalse(arrayOfString.getMethod("foo", true).isPresent());
        assertFalse(arrayOfString.getMethod("foo", false).isPresent());
        assertFalse(arrayOfArrayOfString.getMethod("foo", true).isPresent());
        assertFalse(arrayOfArrayOfString.getMethod("foo", false).isPresent());
    }

    @Test
    public void testSameType() {
        assertEquals(true, arrayOfBoolean.sameType(arrayOfBoolean));
        assertEquals(false, arrayOfBoolean.sameType(arrayOfString));
        assertEquals(false, arrayOfBoolean.sameType(arrayOfArrayOfString));
        assertEquals(false, arrayOfBoolean.sameType(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, arrayOfBoolean.sameType(string));
        assertEquals(false, arrayOfBoolean.sameType(object));
        assertEquals(false, arrayOfString.sameType(arrayOfBoolean));
        assertEquals(true, arrayOfString.sameType(arrayOfString));
        assertEquals(false, arrayOfString.sameType(arrayOfArrayOfString));
        assertEquals(false, arrayOfString.sameType(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, arrayOfString.sameType(string));
        assertEquals(false, arrayOfString.sameType(object));
        assertEquals(false, arrayOfArrayOfString.sameType(arrayOfBoolean));
        assertEquals(false, arrayOfArrayOfString.sameType(arrayOfString));
        assertEquals(true, arrayOfArrayOfString.sameType(arrayOfArrayOfString));
        assertEquals(false, arrayOfArrayOfString.sameType(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, arrayOfArrayOfString.sameType(string));
        assertEquals(false, arrayOfArrayOfString.sameType(object));
    }

    @Test
    public void testCanBeAssignedTo() {
        assertEquals(true, arrayOfBoolean.canBeAssignedTo(arrayOfBoolean));
        assertEquals(false, arrayOfBoolean.canBeAssignedTo(arrayOfString));
        assertEquals(false, arrayOfBoolean.canBeAssignedTo(arrayOfArrayOfString));
        assertEquals(false, arrayOfBoolean.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, arrayOfBoolean.canBeAssignedTo(string));
        assertEquals(true, arrayOfBoolean.canBeAssignedTo(object));
        assertEquals(false, arrayOfString.canBeAssignedTo(arrayOfBoolean));
        assertEquals(true, arrayOfString.canBeAssignedTo(arrayOfString));
        assertEquals(false, arrayOfString.canBeAssignedTo(arrayOfArrayOfString));
        assertEquals(false, arrayOfString.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, arrayOfString.canBeAssignedTo(string));
        assertEquals(true, arrayOfString.canBeAssignedTo(object));
        assertEquals(false, arrayOfArrayOfString.canBeAssignedTo(arrayOfBoolean));
        assertEquals(false, arrayOfArrayOfString.canBeAssignedTo(arrayOfString));
        assertEquals(true, arrayOfArrayOfString.canBeAssignedTo(arrayOfArrayOfString));
        assertEquals(false, arrayOfArrayOfString.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, arrayOfArrayOfString.canBeAssignedTo(string));
        assertEquals(true, arrayOfArrayOfString.canBeAssignedTo(object));
    }

    @Test
    public void testReplaceTypeVariables() {
        assertEquals(arrayOfBoolean, arrayOfBoolean.replaceTypeVariables(Collections.emptyMap()));
        assertEquals(arrayOfString, arrayOfString.replaceTypeVariables(Collections.emptyMap()));
        assertEquals(arrayOfArrayOfString, arrayOfArrayOfString.replaceTypeVariables(Collections.emptyMap()));
        assertEquals(arrayOfBoolean, arrayOfBoolean.replaceTypeVariables(ImmutableMap.of("A", string, "B", object)));
        assertEquals(arrayOfString, arrayOfString.replaceTypeVariables(ImmutableMap.of("A", string, "B", object)));
        assertEquals(arrayOfArrayOfString, arrayOfArrayOfString.replaceTypeVariables(ImmutableMap.of("A", string, "B", object)));
    }

    @Test
    public void testDescribe() {
        assertEquals("array of boolean", arrayOfBoolean.describe());
        assertEquals("array of java.lang.String", arrayOfString.describe());
        assertEquals("array of array of java.lang.String", arrayOfArrayOfString.describe());
    }

}
