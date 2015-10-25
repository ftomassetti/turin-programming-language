package turin.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class ContextTest {

    class MyContext extends Context<String> {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void emptyCase() {
        MyContext ctx = new MyContext();
        assertFalse(ctx.get().isPresent());
        assertFalse(ctx.get().isPresent());
    }

    @Test
    public void simpleCase() {
        MyContext ctx = new MyContext();
        ctx.enterContext("foo");
        assertEquals(Optional.of("foo"), ctx.get());
    }

    @Test
    public void theMostRecentPrevails() {
        MyContext ctx = new MyContext();
        ctx.enterContext("a");
        ctx.enterContext("b");
        ctx.enterContext("c");
        assertEquals(Optional.of("c"), ctx.get());
    }

    @Test
    public void testEnterAndExit() {
        MyContext ctx = new MyContext();
        ctx.enterContext("foo");
        ctx.exitContext();
        assertEquals(Optional.empty(), ctx.get());
    }

    @Test
    public void onExitThePreviousValueIsRestores() {
        MyContext ctx = new MyContext();
        ctx.enterContext("a");
        ctx.enterContext("b");
        ctx.enterContext("c");
        ctx.exitContext();
        ctx.exitContext();
        assertEquals(Optional.of("a"), ctx.get());
    }

}
