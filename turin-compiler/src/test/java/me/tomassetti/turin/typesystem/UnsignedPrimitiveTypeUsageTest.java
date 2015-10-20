package me.tomassetti.turin.typesystem;

import com.google.common.collect.ImmutableMap;
import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.symbols.Symbol;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnsignedPrimitiveTypeUsageTest {

    private ReferenceTypeUsage string;
    private ReferenceTypeUsage object;
    private TypeUsage byteBoxType;
    private TypeUsage shortBoxType;
    private TypeUsage integerBoxType;
    private TypeUsage longBoxType;
    private TypeUsage floatBoxType;
    private TypeUsage doubleBoxType;

    @Before
    public void setup() {
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        string = ReferenceTypeUsage.STRING(resolver);
        object = ReferenceTypeUsage.OBJECT(resolver);
        byteBoxType = ReflectionTypeDefinitionFactory.toTypeUsage(Byte.class, resolver);
        shortBoxType = ReflectionTypeDefinitionFactory.toTypeUsage(Short.class, resolver);
        integerBoxType = ReflectionTypeDefinitionFactory.toTypeUsage(Integer.class, resolver);
        longBoxType = ReflectionTypeDefinitionFactory.toTypeUsage(Long.class, resolver);
        floatBoxType = ReflectionTypeDefinitionFactory.toTypeUsage(Float.class, resolver);
        doubleBoxType = ReflectionTypeDefinitionFactory.toTypeUsage(Double.class, resolver);
    }

    @Test
    public void testIsArray() {
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isArray()));
    }

    @Test
    public void testIsPrimitive() {
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(true, btu.isPrimitive()));
    }

    @Test
    public void testIsReferenceTypeUsage() {
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isReferenceTypeUsage()));
    }

    @Test
    public void testIsVoid() {
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isVoid()));
    }

    @Test
    public void testAsReferenceTypeUsage() {
        int exceptions = 0;
        for (UnsignedPrimitiveTypeUsage btu : UnsignedPrimitiveTypeUsage.ALL) {
            try {
                btu.asReferenceTypeUsage();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(UnsignedPrimitiveTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testAsArrayTypeUsage() {
        int exceptions = 0;
        for (UnsignedPrimitiveTypeUsage btu : UnsignedPrimitiveTypeUsage.ALL) {
            try {
                btu.asArrayTypeUsage();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(UnsignedPrimitiveTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testAsPrimitiveTypeUsage() {
        assertEquals(PrimitiveTypeUsage.BYTE, UnsignedPrimitiveTypeUsage.UBYTE.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.SHORT, UnsignedPrimitiveTypeUsage.USHORT.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.INT, UnsignedPrimitiveTypeUsage.UINT.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.LONG, UnsignedPrimitiveTypeUsage.ULONG.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.FLOAT, UnsignedPrimitiveTypeUsage.UFLOAT.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.DOUBLE, UnsignedPrimitiveTypeUsage.UDOUBLE.asPrimitiveTypeUsage());
    }

    @Test
    public void testIsReference() {
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isReference()));
    }

    @Test
    public void testIsInvokable() {
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isInvokable()));
    }

    @Test
    public void testAsInvokable() {
        int exceptions = 0;
        for (UnsignedPrimitiveTypeUsage btu : UnsignedPrimitiveTypeUsage.ALL) {
            try {
                btu.asInvokable();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(UnsignedPrimitiveTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testJvmType() {
        assertEquals("B", UnsignedPrimitiveTypeUsage.UBYTE.jvmType().getSignature());
        assertEquals("S", UnsignedPrimitiveTypeUsage.USHORT.jvmType().getSignature());
        assertEquals("I", UnsignedPrimitiveTypeUsage.UINT.jvmType().getSignature());
        assertEquals("J", UnsignedPrimitiveTypeUsage.ULONG.jvmType().getSignature());
        assertEquals("F", UnsignedPrimitiveTypeUsage.UFLOAT.jvmType().getSignature());
        assertEquals("D", UnsignedPrimitiveTypeUsage.UDOUBLE.jvmType().getSignature());
    }

    @Test
    public void testHasInstanceField() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.hasInstanceField("foo", instance));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.hasInstanceField("foo", instance));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.hasInstanceField("foo", instance));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.hasInstanceField("foo", instance));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.hasInstanceField("foo", instance));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.hasInstanceField("foo", instance));
        EasyMock.verify(instance);
    }

    @Test
    public void testGetInstanceFieldUnexisting() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        int exceptions = 0;
        for (UnsignedPrimitiveTypeUsage btu : UnsignedPrimitiveTypeUsage.ALL) {
            try {
                btu.getInstanceField("foo", instance);
            } catch (IllegalArgumentException uoe) {
                exceptions++;
            }
        }
        assertEquals(UnsignedPrimitiveTypeUsage.ALL.size(), exceptions);
        EasyMock.verify(instance);
    }

    @Test
    public void testGetMethod() {
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertFalse(btu.getMethod("foo", true).isPresent()));
        UnsignedPrimitiveTypeUsage.ALL.forEach((btu) -> assertFalse(btu.getMethod("foo", false).isPresent()));
    }

    @Test
    public void testSameType() {
        for (int i=0; i< UnsignedPrimitiveTypeUsage.ALL.size(); i++) {
            for (int j=0; j< UnsignedPrimitiveTypeUsage.ALL.size(); j++) {
                assertEquals(i == j, UnsignedPrimitiveTypeUsage.ALL.get(i).sameType(UnsignedPrimitiveTypeUsage.ALL.get(j)));
            }
            for (PrimitiveTypeUsage p : PrimitiveTypeUsage.ALL) {
                assertFalse(UnsignedPrimitiveTypeUsage.ALL.get(i).sameType(p));
            }
            assertFalse(UnsignedPrimitiveTypeUsage.ALL.get(i).sameType(string));
            assertFalse(UnsignedPrimitiveTypeUsage.ALL.get(i).sameType(object));
        }
    }

    @Test
    public void testCanBeAssignedTo() {
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(object));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(string));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UBYTE));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.USHORT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UINT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.ULONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UFLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UDOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(byteBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(shortBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(integerBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UBYTE.canBeAssignedTo(longBoxType));

        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(object));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(string));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UBYTE));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.USHORT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UINT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.ULONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UFLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UDOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(byteBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(shortBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(integerBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.USHORT.canBeAssignedTo(longBoxType));

        assertEquals(true, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(object));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(string));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UBYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.USHORT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UINT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.ULONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UFLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UDOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(byteBoxType));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(shortBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(integerBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UINT.canBeAssignedTo(longBoxType));

        assertEquals(true, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(object));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(string));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UBYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(UnsignedPrimitiveTypeUsage.USHORT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UINT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(UnsignedPrimitiveTypeUsage.ULONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UFLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UDOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(byteBoxType));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(shortBoxType));
        assertEquals(false, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(integerBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.ULONG.canBeAssignedTo(longBoxType));

        assertEquals(true, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(object));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(string));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UBYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.USHORT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UINT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.ULONG));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UFLOAT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UDOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(floatBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UFLOAT.canBeAssignedTo(doubleBoxType));

        assertEquals(true, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(object));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(string));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UBYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.USHORT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UINT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.ULONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UFLOAT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(UnsignedPrimitiveTypeUsage.UDOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(floatBoxType));
        assertEquals(true, UnsignedPrimitiveTypeUsage.UDOUBLE.canBeAssignedTo(doubleBoxType));
    }

    @Test
    public void testReplaceTypeVariables() {
        for (UnsignedPrimitiveTypeUsage ptu : UnsignedPrimitiveTypeUsage.ALL) {
            assertTrue(ptu == ptu.replaceTypeVariables(Collections.emptyMap()));
            assertTrue(ptu == ptu.replaceTypeVariables(ImmutableMap.of("A", string, "B", object)));
        }
    }

    @Test
    public void testDescribe() {
        assertEquals("ubyte", UnsignedPrimitiveTypeUsage.UBYTE.describe());
        assertEquals("ushort", UnsignedPrimitiveTypeUsage.USHORT.describe());
        assertEquals("uint", UnsignedPrimitiveTypeUsage.UINT.describe());
        assertEquals("ulong", UnsignedPrimitiveTypeUsage.ULONG.describe());
        assertEquals("ufloat", UnsignedPrimitiveTypeUsage.UFLOAT.describe());
        assertEquals("udouble", UnsignedPrimitiveTypeUsage.UDOUBLE.describe());
    }

}
