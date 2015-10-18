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

}
