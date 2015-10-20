package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.symbols.Symbol;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BasicTypeUsageTest {

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
        BasicTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isArray()));
    }

    @Test
    public void testIsPrimitive() {
        BasicTypeUsage.ALL.forEach((btu) -> assertEquals(true, btu.isPrimitive()));
    }

    @Test
    public void testIsReferenceTypeUsage() {
        BasicTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isReferenceTypeUsage()));
    }

    @Test
    public void testIsVoid() {
        BasicTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isVoid()));
    }

    @Test
    public void testAsReferenceTypeUsage() {
        int exceptions = 0;
        for (BasicTypeUsage btu : BasicTypeUsage.ALL) {
            try {
                btu.asReferenceTypeUsage();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(BasicTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testAsArrayTypeUsage() {
        int exceptions = 0;
        for (BasicTypeUsage btu : BasicTypeUsage.ALL) {
            try {
                btu.asArrayTypeUsage();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(BasicTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testAsPrimitiveTypeUsage() {
        assertEquals(PrimitiveTypeUsage.BYTE, BasicTypeUsage.UBYTE.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.SHORT, BasicTypeUsage.USHORT.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.INT, BasicTypeUsage.UINT.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.LONG, BasicTypeUsage.ULONG.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.FLOAT, BasicTypeUsage.UFLOAT.asPrimitiveTypeUsage());
        assertEquals(PrimitiveTypeUsage.DOUBLE, BasicTypeUsage.UDOUBLE.asPrimitiveTypeUsage());
    }

    @Test
    public void testIsReference() {
        BasicTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isReference()));
    }

    @Test
    public void testIsInvokable() {
        BasicTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isInvokable()));
    }

    @Test
    public void testAsInvokable() {
        int exceptions = 0;
        for (BasicTypeUsage btu : BasicTypeUsage.ALL) {
            try {
                btu.asInvokable();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(BasicTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testJvmType() {
        assertEquals("B", BasicTypeUsage.UBYTE.jvmType().getSignature());
        assertEquals("S", BasicTypeUsage.USHORT.jvmType().getSignature());
        assertEquals("I", BasicTypeUsage.UINT.jvmType().getSignature());
        assertEquals("J", BasicTypeUsage.ULONG.jvmType().getSignature());
        assertEquals("F", BasicTypeUsage.UFLOAT.jvmType().getSignature());
        assertEquals("D", BasicTypeUsage.UDOUBLE.jvmType().getSignature());
    }

    @Test
    public void testHasInstanceField() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        assertEquals(false, BasicTypeUsage.UBYTE.hasInstanceField("foo", instance));
        assertEquals(false, BasicTypeUsage.USHORT.hasInstanceField("foo", instance));
        assertEquals(false, BasicTypeUsage.UINT.hasInstanceField("foo", instance));
        assertEquals(false, BasicTypeUsage.ULONG.hasInstanceField("foo", instance));
        assertEquals(false, BasicTypeUsage.UFLOAT.hasInstanceField("foo", instance));
        assertEquals(false, BasicTypeUsage.UDOUBLE.hasInstanceField("foo", instance));
        EasyMock.verify(instance);
    }

    @Test
    public void testGetInstanceFieldUnexisting() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        int exceptions = 0;
        for (BasicTypeUsage btu : BasicTypeUsage.ALL) {
            try {
                btu.getInstanceField("foo", instance);
            } catch (IllegalArgumentException uoe) {
                exceptions++;
            }
        }
        assertEquals(BasicTypeUsage.ALL.size(), exceptions);
        EasyMock.verify(instance);
    }

    @Test
    public void testGetMethod() {
        BasicTypeUsage.ALL.forEach((btu) -> assertFalse(btu.getMethod("foo", true).isPresent()));
        BasicTypeUsage.ALL.forEach((btu) -> assertFalse(btu.getMethod("foo", false).isPresent()));
    }

    @Test
    public void testSameType() {
        for (int i=0; i<BasicTypeUsage.ALL.size(); i++) {
            for (int j=0; j<BasicTypeUsage.ALL.size(); j++) {
                assertEquals(i == j, BasicTypeUsage.ALL.get(i).sameType(BasicTypeUsage.ALL.get(j)));
            }
            for (PrimitiveTypeUsage p : PrimitiveTypeUsage.ALL) {
                assertFalse(BasicTypeUsage.ALL.get(i).sameType(p));
            }
            assertFalse(BasicTypeUsage.ALL.get(i).sameType(string));
            assertFalse(BasicTypeUsage.ALL.get(i).sameType(object));
        }
    }

    @Test
    public void testCanBeAssignedTo() {
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(object));
        assertEquals(false, BasicTypeUsage.UBYTE.canBeAssignedTo(string));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(BasicTypeUsage.UBYTE));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(BasicTypeUsage.USHORT));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(BasicTypeUsage.UINT));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(BasicTypeUsage.ULONG));
        assertEquals(false, BasicTypeUsage.UBYTE.canBeAssignedTo(BasicTypeUsage.UFLOAT));
        assertEquals(false, BasicTypeUsage.UBYTE.canBeAssignedTo(BasicTypeUsage.UDOUBLE));
        assertEquals(false, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, BasicTypeUsage.UBYTE.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(byteBoxType));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(shortBoxType));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(integerBoxType));
        assertEquals(true, BasicTypeUsage.UBYTE.canBeAssignedTo(longBoxType));

        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(object));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(string));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(BasicTypeUsage.UBYTE));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(BasicTypeUsage.USHORT));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(BasicTypeUsage.UINT));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(BasicTypeUsage.ULONG));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(BasicTypeUsage.UFLOAT));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(BasicTypeUsage.UDOUBLE));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, BasicTypeUsage.USHORT.canBeAssignedTo(byteBoxType));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(shortBoxType));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(integerBoxType));
        assertEquals(true, BasicTypeUsage.USHORT.canBeAssignedTo(longBoxType));

        assertEquals(true, BasicTypeUsage.UINT.canBeAssignedTo(object));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(string));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(BasicTypeUsage.UBYTE));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(BasicTypeUsage.USHORT));
        assertEquals(true, BasicTypeUsage.UINT.canBeAssignedTo(BasicTypeUsage.UINT));
        assertEquals(true, BasicTypeUsage.UINT.canBeAssignedTo(BasicTypeUsage.ULONG));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(BasicTypeUsage.UFLOAT));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(BasicTypeUsage.UDOUBLE));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(byteBoxType));
        assertEquals(false, BasicTypeUsage.UINT.canBeAssignedTo(shortBoxType));
        assertEquals(true, BasicTypeUsage.UINT.canBeAssignedTo(integerBoxType));
        assertEquals(true, BasicTypeUsage.UINT.canBeAssignedTo(longBoxType));

        assertEquals(true, BasicTypeUsage.ULONG.canBeAssignedTo(object));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(string));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(BasicTypeUsage.UBYTE));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(BasicTypeUsage.USHORT));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(BasicTypeUsage.UINT));
        assertEquals(true, BasicTypeUsage.ULONG.canBeAssignedTo(BasicTypeUsage.ULONG));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(BasicTypeUsage.UFLOAT));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(BasicTypeUsage.UDOUBLE));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(byteBoxType));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(shortBoxType));
        assertEquals(false, BasicTypeUsage.ULONG.canBeAssignedTo(integerBoxType));
        assertEquals(true, BasicTypeUsage.ULONG.canBeAssignedTo(longBoxType));

        assertEquals(true, BasicTypeUsage.UFLOAT.canBeAssignedTo(object));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(string));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(BasicTypeUsage.UBYTE));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(BasicTypeUsage.USHORT));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(BasicTypeUsage.UINT));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(BasicTypeUsage.ULONG));
        assertEquals(true, BasicTypeUsage.UFLOAT.canBeAssignedTo(BasicTypeUsage.UFLOAT));
        assertEquals(true, BasicTypeUsage.UFLOAT.canBeAssignedTo(BasicTypeUsage.UDOUBLE));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(false, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(true, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(true, BasicTypeUsage.UFLOAT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(true, BasicTypeUsage.UFLOAT.canBeAssignedTo(floatBoxType));
        assertEquals(true, BasicTypeUsage.UFLOAT.canBeAssignedTo(doubleBoxType));

        assertEquals(true, BasicTypeUsage.UDOUBLE.canBeAssignedTo(object));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(string));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(BasicTypeUsage.UBYTE));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(BasicTypeUsage.USHORT));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(BasicTypeUsage.UINT));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(BasicTypeUsage.ULONG));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(BasicTypeUsage.UFLOAT));
        assertEquals(true, BasicTypeUsage.UDOUBLE.canBeAssignedTo(BasicTypeUsage.UDOUBLE));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(true, BasicTypeUsage.UDOUBLE.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, BasicTypeUsage.UDOUBLE.canBeAssignedTo(floatBoxType));
        assertEquals(true, BasicTypeUsage.UDOUBLE.canBeAssignedTo(doubleBoxType));
    }

    /*@Test
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
    }*/

}
