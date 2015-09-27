package me.tomassetti.turin;

import me.tomassetti.turin.util.Either;
import org.junit.Test;

import static org.junit.Assert.*;

public class EitherTest {

    Either ok = Either.right("ok");
    Either ko = Either.left("ko");

    @Test
    public void getRightPositive() {
        assertEquals("ok", ok.getRight());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRightNegative() {
        ko.getRight();
    }

    @Test
    public void getLeftPositive() {
        assertEquals("ko", ko.getLeft());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLeftNegative() {
        ok.getLeft();
    }

    @Test
    public void testIsLeft() {
        assertEquals(false, ok.isLeft());
        assertEquals(true, ko.isLeft());
    }

    @Test
    public void testIsRight() {
        assertEquals(true, ok.isRight());
        assertEquals(false, ko.isRight());
    }

    @Test
    public void testToString() {
        assertEquals("Either.right(ok)", ok.toString());
        assertEquals("Either.left(ko)", ko.toString());
    }

}
