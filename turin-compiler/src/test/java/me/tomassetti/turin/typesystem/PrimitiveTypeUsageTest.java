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

import static org.junit.Assert.*;

public class PrimitiveTypeUsageTest {

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
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isArray()));
    }

    @Test
    public void testIsPrimitive() {
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(true, btu.isPrimitive()));
    }

    @Test
    public void testIsReferenceTypeUsage() {
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isReferenceTypeUsage()));
    }

    @Test
    public void testIsVoid() {
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isVoid()));
    }

    @Test
    public void testAsReferenceTypeUsage() {
        int exceptions = 0;
        for (PrimitiveTypeUsage btu : PrimitiveTypeUsage.ALL) {
            try {
                btu.asReferenceTypeUsage();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(PrimitiveTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testAsArrayTypeUsage() {
        int exceptions = 0;
        for (PrimitiveTypeUsage btu : PrimitiveTypeUsage.ALL) {
            try {
                btu.asArrayTypeUsage();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(PrimitiveTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testAsPrimitiveTypeUsage() {
        PrimitiveTypeUsage.ALL.forEach((ptu) -> assertTrue(ptu == ptu.asPrimitiveTypeUsage()));
    }

    @Test
    public void testIsReference() {
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isReference()));
    }

    @Test
    public void testIsInvokable() {
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertEquals(false, btu.isInvokable()));
    }

    @Test
    public void testAsInvokable() {
        int exceptions = 0;
        for (PrimitiveTypeUsage btu : PrimitiveTypeUsage.ALL) {
            try {
                btu.asInvokable();
            } catch (UnsupportedOperationException uoe) {
                exceptions++;
            }
        }
        assertEquals(PrimitiveTypeUsage.ALL.size(), exceptions);
    }

    @Test
    public void testJvmType() {
        assertEquals("B", PrimitiveTypeUsage.BYTE.jvmType().getSignature());
        assertEquals("S", PrimitiveTypeUsage.SHORT.jvmType().getSignature());
        assertEquals("I", PrimitiveTypeUsage.INT.jvmType().getSignature());
        assertEquals("J", PrimitiveTypeUsage.LONG.jvmType().getSignature());
        assertEquals("F", PrimitiveTypeUsage.FLOAT.jvmType().getSignature());
        assertEquals("D", PrimitiveTypeUsage.DOUBLE.jvmType().getSignature());
    }

    @Test
    public void testHasInstanceField() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        assertEquals(false, PrimitiveTypeUsage.BYTE.hasInstanceField("foo", instance));
        assertEquals(false, PrimitiveTypeUsage.SHORT.hasInstanceField("foo", instance));
        assertEquals(false, PrimitiveTypeUsage.INT.hasInstanceField("foo", instance));
        assertEquals(false, PrimitiveTypeUsage.LONG.hasInstanceField("foo", instance));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.hasInstanceField("foo", instance));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.hasInstanceField("foo", instance));
        EasyMock.verify(instance);
    }

    @Test
    public void testGetInstanceFieldUnexisting() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        int exceptions = 0;
        for (PrimitiveTypeUsage btu : PrimitiveTypeUsage.ALL) {
            try {
                btu.getInstanceField("foo", instance);
            } catch (IllegalArgumentException uoe) {
                exceptions++;
            }
        }
        assertEquals(PrimitiveTypeUsage.ALL.size(), exceptions);
        EasyMock.verify(instance);
    }

    @Test
    public void testGetMethod() {
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertFalse(btu.getMethod("foo", true).isPresent()));
        PrimitiveTypeUsage.ALL.forEach((btu) -> assertFalse(btu.getMethod("foo", false).isPresent()));
    }

    @Test
    public void testSameType() {
        for (int i=0; i<PrimitiveTypeUsage.ALL.size(); i++) {
            for (int j=0; j<PrimitiveTypeUsage.ALL.size(); j++) {
                assertEquals(i == j, PrimitiveTypeUsage.ALL.get(i).sameType(PrimitiveTypeUsage.ALL.get(j)));
            }
            for (UnsignedPrimitiveTypeUsage btu : UnsignedPrimitiveTypeUsage.ALL) {
                assertFalse(PrimitiveTypeUsage.ALL.get(i).describe() + " sameType as " + btu.describe(),
                        PrimitiveTypeUsage.ALL.get(i).sameType(btu));
            }
            assertFalse(PrimitiveTypeUsage.ALL.get(i).sameType(string));
            assertFalse(PrimitiveTypeUsage.ALL.get(i).sameType(object));
        }
    }

    @Test
    public void testCanBeAssignedTo() {
        for (PrimitiveTypeUsage ptu : PrimitiveTypeUsage.ALL) {
            for (UnsignedPrimitiveTypeUsage btu : UnsignedPrimitiveTypeUsage.ALL) {
                assertFalse(ptu.describe() + " can be assigned to " + btu.describe(), ptu.canBeAssignedTo(btu));
            }
        }

        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(object));
        assertEquals(false, PrimitiveTypeUsage.BYTE.canBeAssignedTo(string));
        assertEquals(false, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, PrimitiveTypeUsage.BYTE.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(byteBoxType));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(shortBoxType));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(integerBoxType));
        assertEquals(true, PrimitiveTypeUsage.BYTE.canBeAssignedTo(longBoxType));

        assertEquals(true, PrimitiveTypeUsage.SHORT.canBeAssignedTo(object));
        assertEquals(false, PrimitiveTypeUsage.SHORT.canBeAssignedTo(string));
        assertEquals(false, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(true, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, PrimitiveTypeUsage.SHORT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, PrimitiveTypeUsage.SHORT.canBeAssignedTo(byteBoxType));
        assertEquals(true, PrimitiveTypeUsage.SHORT.canBeAssignedTo(shortBoxType));
        assertEquals(true, PrimitiveTypeUsage.SHORT.canBeAssignedTo(integerBoxType));
        assertEquals(true, PrimitiveTypeUsage.SHORT.canBeAssignedTo(longBoxType));

        assertEquals(true, PrimitiveTypeUsage.INT.canBeAssignedTo(object));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(string));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(true, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(byteBoxType));
        assertEquals(false, PrimitiveTypeUsage.INT.canBeAssignedTo(shortBoxType));
        assertEquals(true, PrimitiveTypeUsage.INT.canBeAssignedTo(integerBoxType));
        assertEquals(true, PrimitiveTypeUsage.INT.canBeAssignedTo(longBoxType));

        assertEquals(true, PrimitiveTypeUsage.LONG.canBeAssignedTo(object));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(string));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(true, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(byteBoxType));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(shortBoxType));
        assertEquals(false, PrimitiveTypeUsage.LONG.canBeAssignedTo(integerBoxType));
        assertEquals(true, PrimitiveTypeUsage.LONG.canBeAssignedTo(longBoxType));

        assertEquals(true, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(object));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(string));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(false, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(true, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(true, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(true, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(floatBoxType));
        assertEquals(true, PrimitiveTypeUsage.FLOAT.canBeAssignedTo(doubleBoxType));

        assertEquals(true, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(object));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(string));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.CHAR));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.BOOLEAN));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.BYTE));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.SHORT));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.INT));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.LONG));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.FLOAT));
        assertEquals(true, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(PrimitiveTypeUsage.DOUBLE));
        assertEquals(false, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(floatBoxType));
        assertEquals(true, PrimitiveTypeUsage.DOUBLE.canBeAssignedTo(doubleBoxType));
    }

    @Test
    public void testReplaceTypeVariables() {
        for (PrimitiveTypeUsage ptu : PrimitiveTypeUsage.ALL) {
            assertTrue(ptu == ptu.replaceTypeVariables(Collections.emptyMap()));
            assertTrue(ptu == ptu.replaceTypeVariables(ImmutableMap.of("A", string, "B", object)));
        }
    }

    @Test
    public void testDescribe() {
        assertEquals("boolean", PrimitiveTypeUsage.BOOLEAN.describe());
        assertEquals("char", PrimitiveTypeUsage.CHAR.describe());
        assertEquals("byte", PrimitiveTypeUsage.BYTE.describe());
        assertEquals("short", PrimitiveTypeUsage.SHORT.describe());
        assertEquals("int", PrimitiveTypeUsage.INT.describe());
        assertEquals("long", PrimitiveTypeUsage.LONG.describe());
        assertEquals("float", PrimitiveTypeUsage.FLOAT.describe());
        assertEquals("double", PrimitiveTypeUsage.DOUBLE.describe());
    }

}
