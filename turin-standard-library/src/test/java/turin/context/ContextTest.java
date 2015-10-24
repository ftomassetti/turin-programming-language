package turin.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class ContextTest {
    @After
    public void tearDown() throws Exception {
        while (!Context.isEmpty()) {
            Context.exitContext();
        }
    }

    @Before
    public void setUp() throws Exception {
        while (!Context.isEmpty()) {
            Context.exitContext();
        }
    }

    @Test
    public void emptyCase() {
        assertFalse(Context.get("foo").isPresent());
        assertFalse(Context.get("bar").isPresent());
    }

    @Test
    public void simpleCase() {
        Map<String, Object> data = new HashMap<>();
        data.put("foo", 123);
        Context.enterContext(data);
        assertEquals(Optional.of(123), Context.get("foo"));
    }

    @Test
    public void testEnterAndExit() {
        Map<String, Object> data = new HashMap<>();
        data.put("foo", 123);
        Context.enterContext(data);
        Context.exitContext();
        assertEquals(Optional.empty(), Context.get("foo"));
    }

}
