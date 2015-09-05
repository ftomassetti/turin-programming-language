package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class QualifiedNameTest {

    @Test(expected = IllegalArgumentException.class)
    public void createWithEmptyList() {
        QualifiedName.create(Collections.emptyList());
    }

    @Test
    public void createWithOneSegment() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello"));
        assertEquals(true, qualifiedName.isSimpleName());
        assertEquals("hello", qualifiedName.qualifiedName());
    }

    @Test
    public void createWithTwoSegments() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello", "turin"));
        assertEquals(false, qualifiedName.isSimpleName());
        assertEquals("hello.turin", qualifiedName.qualifiedName());
    }

    @Test
    public void createWithManySegments() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello", "turin", "how", "are", "you"));
        assertEquals(false, qualifiedName.isSimpleName());
        assertEquals("hello.turin.how.are.you", qualifiedName.qualifiedName());
    }

    @Test
    public void firstSegmentWithOneSegment() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello"));
        assertEquals("hello", qualifiedName.firstSegment());
    }

    @Test
    public void firstSegmentWithTwoSegments() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello", "turin"));
        assertEquals("hello", qualifiedName.firstSegment());
    }

    @Test
    public void firstSegmentWithManySegments() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello", "turin", "how", "are", "you"));
        assertEquals("hello", qualifiedName.firstSegment());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void restWithOneSegment() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello"));
        qualifiedName.rest();
    }

    @Test
    public void restWithTwoSegments() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello", "turin"));
        assertEquals(QualifiedName.create(ImmutableList.of("turin")), qualifiedName.rest());
    }

    @Test
    public void restWithManySegments() {
        QualifiedName qualifiedName = QualifiedName.create(ImmutableList.of("hello", "turin", "how", "are", "you"));
        assertEquals(QualifiedName.create(ImmutableList.of("turin", "how", "are", "you")), qualifiedName.rest());
    }

}
