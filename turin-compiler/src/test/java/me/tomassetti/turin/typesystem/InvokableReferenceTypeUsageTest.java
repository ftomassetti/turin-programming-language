package me.tomassetti.turin.typesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.turin.definitions.InternalConstructorDefinition;
import me.tomassetti.turin.definitions.InternalInvokableDefinition;
import me.tomassetti.turin.definitions.InternalMethodDefinition;
import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.symbols.FormalParameterSymbol;
import me.tomassetti.turin.symbols.Symbol;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class InvokableReferenceTypeUsageTest {

    private InvokableReferenceTypeUsage invokableA;
    private InvokableReferenceTypeUsage invokableB;
    private InvokableReferenceTypeUsage invokableC;
    private InvokableReferenceTypeUsage invokableD;
    private InvokableReferenceTypeUsage invokableSameAsA;
    private InvokableReferenceTypeUsage invokableSameAsD;
    private ReferenceTypeUsage string;
    private ReferenceTypeUsage object;

    @Before
    public void setup() {
        SymbolResolver resolver = new InFileSymbolResolver(JdkTypeResolver.getInstance());
        object = ReferenceTypeUsage.OBJECT(resolver);
        string = ReferenceTypeUsage.STRING(resolver);
        InternalInvokableDefinition internalA = new InternalMethodDefinition(
                "print",
                ImmutableList.of(
                        new FormalParameterSymbol(PrimitiveTypeUsage.INT, "n"),
                        new FormalParameterSymbol(object, "what")),
                string,
                new JvmMethodDefinition("a/b/MyClass", "print", "(ILjava/lang/Object;)Ljava/lang/String;", false, false));
        invokableA = new InvokableReferenceTypeUsage(internalA);
        InternalInvokableDefinition internalB = new InternalMethodDefinition(
                "foo",
                ImmutableList.of(
                        new FormalParameterSymbol(PrimitiveTypeUsage.BOOLEAN, "flag")),
                new VoidTypeUsage(),
                new JvmMethodDefinition("a/b/MyClass", "foo", "(Z)V", true, false));
        invokableB = new InvokableReferenceTypeUsage(internalB);
        InternalInvokableDefinition internalC = new InternalMethodDefinition(
                "bar",
                ImmutableList.of(),
                new ConcreteTypeVariableUsage(TypeVariableUsage.GenericDeclaration.onClass("a.b.MyClass"), "A", Collections.emptyList()),
                // where A is a Type Variable
                new JvmMethodDefinition("a/b/MyClass", "internalC", "()A", false, true));
        invokableC = new InvokableReferenceTypeUsage(internalC);
        InternalInvokableDefinition internalD = new InternalConstructorDefinition(
                string,
                ImmutableList.of(),
                new JvmConstructorDefinition("a/b/MyClass", "()Ljava/lang/string;"));
        invokableD = new InvokableReferenceTypeUsage(internalD);
        InternalInvokableDefinition internalSameAsA = new InternalConstructorDefinition(
                string,
                ImmutableList.of(
                        new FormalParameterSymbol(PrimitiveTypeUsage.INT, "n"),
                        new FormalParameterSymbol(object, "what")),
                new JvmConstructorDefinition("a/b/AnotherClass", "(ILjava/lang/Object;)Ljava/lang/String;"));
        invokableSameAsA = new InvokableReferenceTypeUsage(internalSameAsA);
        InternalInvokableDefinition internalSameAsD = new InternalConstructorDefinition(
                string,
                ImmutableList.of(),
                new JvmConstructorDefinition("a/b/AnotherClass", "()V"));
        invokableSameAsD = new InvokableReferenceTypeUsage(internalSameAsD);
    }

    @Test
    public void testIsArray() {
        assertEquals(false, invokableA.isArray());
        assertEquals(false, invokableB.isArray());
        assertEquals(false, invokableC.isArray());
        assertEquals(false, invokableD.isArray());
    }

    @Test
    public void testIsPrimitive() {
        assertEquals(false, invokableA.isPrimitive());
        assertEquals(false, invokableB.isPrimitive());
        assertEquals(false, invokableC.isPrimitive());
        assertEquals(false, invokableD.isPrimitive());
    }

    @Test
    public void testIsReferenceTypeUsage() {
        assertEquals(false, invokableA.isReferenceTypeUsage());
        assertEquals(false, invokableB.isReferenceTypeUsage());
        assertEquals(false, invokableC.isReferenceTypeUsage());
        assertEquals(false, invokableD.isReferenceTypeUsage());
    }

    @Test
    public void testIsVoid() {
        assertEquals(false, invokableA.isVoid());
        assertEquals(false, invokableB.isVoid());
        assertEquals(false, invokableC.isVoid());
        assertEquals(false, invokableD.isVoid());
    }

    @Test
    public void testAsReferenceTypeUsage() {
        int exceptions = 0;
        try {
            invokableA.asReferenceTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableB.asReferenceTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableC.asReferenceTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableD.asReferenceTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        assertEquals(4, exceptions);
    }

    @Test
    public void testAsArrayTypeUsage() {
        int exceptions = 0;
        try {
            invokableA.asArrayTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableB.asArrayTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableC.asArrayTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableD.asArrayTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        assertEquals(4, exceptions);
    }

    @Test
    public void testAsPrimitiveTypeUsage() {
        int exceptions = 0;
        try {
            invokableA.asPrimitiveTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableB.asPrimitiveTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableC.asPrimitiveTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableD.asPrimitiveTypeUsage();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        assertEquals(4, exceptions);
    }

    @Test
    public void testIsReference() {
        assertEquals(false, invokableA.isReference());
        assertEquals(false, invokableB.isReference());
        assertEquals(false, invokableC.isReference());
        assertEquals(false, invokableD.isReference());
    }

    @Test
    public void testIsInvokable() {
        assertEquals(true, invokableA.isInvokable());
        assertEquals(true, invokableB.isInvokable());
        assertEquals(true, invokableC.isInvokable());
        assertEquals(true, invokableD.isInvokable());
    }

    @Test
    public void testAsInvokable() {
        assertTrue(invokableA == invokableA.asInvokable());
        assertTrue(invokableB == invokableB.asInvokable());
        assertTrue(invokableC == invokableC.asInvokable());
        assertTrue(invokableD == invokableD.asInvokable());
    }

    @Test
    public void testJvmType() {
        int exceptions = 0;
        try {
            invokableA.jvmType();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableB.jvmType();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableC.jvmType();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        try {
            invokableD.jvmType();
        } catch (UnsupportedOperationException uoe) {
            exceptions++;
        }
        assertEquals(4, exceptions);
    }

    @Test
    public void testHasInstanceField() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        assertEquals(false, invokableA.hasInstanceField("foo", instance));
        assertEquals(false, invokableB.hasInstanceField("foo", instance));
        assertEquals(false, invokableC.hasInstanceField("foo", instance));
        assertEquals(false, invokableD.hasInstanceField("foo", instance));
        EasyMock.verify(instance);
    }

    @Test
    public void testGetInstanceFieldUnexisting() {
        Symbol instance = EasyMock.createMock(Symbol.class);
        EasyMock.replay(instance);
        int exceptions = 0;
        try {
            invokableA.getInstanceField("foo", instance);
        } catch (IllegalArgumentException uoe) {
            exceptions++;
        }
        try {
            invokableB.getInstanceField("foo", instance);
        } catch (IllegalArgumentException uoe) {
            exceptions++;
        }
        try {
            invokableC.getInstanceField("foo", instance);
        } catch (IllegalArgumentException uoe) {
            exceptions++;
        }
        try {
            invokableD.getInstanceField("foo", instance);
        } catch (IllegalArgumentException uoe) {
            exceptions++;
        }
        assertEquals(4, exceptions);
        EasyMock.verify(instance);
    }

    @Test
    public void testGetMethod() {
        assertFalse(invokableA.getMethod("foo", false).isPresent());
        assertFalse(invokableB.getMethod("foo", false).isPresent());
        assertFalse(invokableC.getMethod("foo", false).isPresent());
        assertFalse(invokableD.getMethod("foo", false).isPresent());
        assertFalse(invokableA.getMethod("foo", true).isPresent());
        assertFalse(invokableB.getMethod("foo", true).isPresent());
        assertFalse(invokableC.getMethod("foo", true).isPresent());
        assertFalse(invokableD.getMethod("foo", true).isPresent());
    }

    @Test
    public void testSameType() {
        assertEquals(true, invokableA.sameType(invokableA));
        assertEquals(false, invokableA.sameType(invokableB));
        assertEquals(false, invokableA.sameType(invokableC));
        assertEquals(false, invokableA.sameType(invokableD));
        assertEquals(true, invokableA.sameType(invokableSameAsA));
        assertEquals(false, invokableA.sameType(invokableSameAsD));

        assertEquals(false, invokableB.sameType(invokableA));
        assertEquals(true, invokableB.sameType(invokableB));
        assertEquals(false, invokableB.sameType(invokableC));
        assertEquals(false, invokableB.sameType(invokableD));
        assertEquals(false, invokableB.sameType(invokableSameAsA));
        assertEquals(false, invokableB.sameType(invokableSameAsD));

        assertEquals(false, invokableC.sameType(invokableA));
        assertEquals(false, invokableC.sameType(invokableB));
        assertEquals(true, invokableC.sameType(invokableC));
        assertEquals(false, invokableC.sameType(invokableD));
        assertEquals(false, invokableC.sameType(invokableSameAsA));
        assertEquals(false, invokableC.sameType(invokableSameAsD));

        assertEquals(false, invokableD.sameType(invokableA));
        assertEquals(false, invokableD.sameType(invokableB));
        assertEquals(false, invokableD.sameType(invokableC));
        assertEquals(true, invokableD.sameType(invokableD));
        assertEquals(false, invokableD.sameType(invokableSameAsA));
        assertEquals(true, invokableD.sameType(invokableSameAsD));
    }

    @Test
    public void testCanBeAssignedTo() {
        assertFalse(invokableA.canBeAssignedTo(invokableA));
        assertFalse(invokableA.canBeAssignedTo(invokableB));
        assertFalse(invokableA.canBeAssignedTo(invokableC));
        assertFalse(invokableA.canBeAssignedTo(invokableD));
        assertFalse(invokableA.canBeAssignedTo(object));
        assertFalse(invokableA.canBeAssignedTo(string));

        assertFalse(invokableB.canBeAssignedTo(invokableA));
        assertFalse(invokableB.canBeAssignedTo(invokableB));
        assertFalse(invokableB.canBeAssignedTo(invokableC));
        assertFalse(invokableB.canBeAssignedTo(invokableD));
        assertFalse(invokableB.canBeAssignedTo(object));
        assertFalse(invokableB.canBeAssignedTo(string));

        assertFalse(invokableC.canBeAssignedTo(invokableA));
        assertFalse(invokableC.canBeAssignedTo(invokableB));
        assertFalse(invokableC.canBeAssignedTo(invokableC));
        assertFalse(invokableC.canBeAssignedTo(invokableD));
        assertFalse(invokableC.canBeAssignedTo(object));
        assertFalse(invokableC.canBeAssignedTo(string));

        assertFalse(invokableD.canBeAssignedTo(invokableA));
        assertFalse(invokableD.canBeAssignedTo(invokableB));
        assertFalse(invokableD.canBeAssignedTo(invokableC));
        assertFalse(invokableD.canBeAssignedTo(invokableD));
        assertFalse(invokableD.canBeAssignedTo(object));
        assertFalse(invokableD.canBeAssignedTo(string));
    }

    @Test
    public void testReplaceTypeVariables() {
        Map<String, TypeUsage> typeParams = ImmutableMap.of("A", string, "B", object);
        assertEquals(invokableA, invokableA.replaceTypeVariables(Collections.emptyMap()));
        assertEquals(invokableA, invokableA.replaceTypeVariables(typeParams));
        assertEquals(invokableB, invokableB.replaceTypeVariables(Collections.emptyMap()));
        assertEquals(invokableB, invokableB.replaceTypeVariables(typeParams));
        assertEquals(invokableC, invokableC.replaceTypeVariables(Collections.emptyMap()));
        assertNotEquals(invokableC, invokableC.replaceTypeVariables(typeParams));
        TypeUsage invokableCReplaced = invokableC.replaceTypeVariables(typeParams);
        InternalInvokableDefinition internalCReplaced = new InternalMethodDefinition(
                "bar",
                ImmutableList.of(),
                string,
                // where A is a Type Variable
                new JvmMethodDefinition("a/b/MyClass", "internalC", "()A", false, true));
        assertEquals(new InvokableReferenceTypeUsage(internalCReplaced), invokableCReplaced);
        assertEquals(invokableD, invokableD.replaceTypeVariables(Collections.emptyMap()));
        assertEquals(invokableD, invokableD.replaceTypeVariables(typeParams));
    }

    @Test
    public void testDescribe() {
        assertEquals("(int, java.lang.Object) -> java.lang.String", invokableA.describe());
        assertEquals("(boolean) -> void", invokableB.describe());
        assertEquals("() -> type variable A", invokableC.describe());
        assertEquals("() -> java.lang.String", invokableD.describe());
    }

    //Optional<? extends InternalInvokableDefinition> internalInvokableDefinitionFor(List<ActualParam> actualParams);

    //boolean isOverloaded();

}
