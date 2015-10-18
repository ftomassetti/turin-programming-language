package me.tomassetti.turin.typesystem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VoidTypeUsageTest {

    @Test
    public void testIsArray() {
        assertEquals(false, new VoidTypeUsage().isArray());
    }

    @Test
    public void testIsPrimitive() {
        assertEquals(false, new VoidTypeUsage().isPrimitive());
    }

}
