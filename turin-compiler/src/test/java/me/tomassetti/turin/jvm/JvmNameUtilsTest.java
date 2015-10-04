package me.tomassetti.turin.jvm;

import me.tomassetti.jvm.JvmNameUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class JvmNameUtilsTest {

    @Test
    public void isValidQualifiedNamePositiveCase(){
        assertEquals(true, JvmNameUtils.isValidQualifiedName("me.tomassetti.turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceStartingWithDigit(){
        assertEquals(false, JvmNameUtils.isValidQualifiedName("me.0tomassetti.turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceEmptyInTheMiddle(){
        assertEquals(false, JvmNameUtils.isValidQualifiedName("me..turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceEmptyAtStart(){
        assertEquals(false, JvmNameUtils.isValidQualifiedName(".tomassetti.turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceEmptyAtEnd(){
        assertEquals(false, JvmNameUtils.isValidQualifiedName("me.tomassetti."));
    }

    @Test
    public void isValidJavaIdentifierStartingWithDigit(){
        assertEquals(false, JvmNameUtils.isValidJavaIdentifier("3foo"));
    }

    @Test
    public void isValidJavaIdentifierContainingDollar(){
        assertEquals(true, JvmNameUtils.isValidJavaIdentifier("fo$o"));
    }

    @Test
    public void isValidJavaIdentifierMadeOnlyByDollarsAndUnderscores(){
        assertEquals(true, JvmNameUtils.isValidJavaIdentifier("_$__$$"));
    }

    @Test
    public void isValidJavaIdentifierContainingPercent(){
        assertEquals(false, JvmNameUtils.isValidJavaIdentifier("foo%zilla"));
    }

    @Test
    public void isValidJavaIdentifierContainingSpace(){
        assertEquals(false, JvmNameUtils.isValidJavaIdentifier("foo zilla"));
    }

    @Test
    public void isValidJavaIdentifierCorrespondToKeyword(){
        assertEquals(false, JvmNameUtils.isValidJavaIdentifier("for"));
    }

    @Test
    public void isValidJavaIdentifierCorrespondToKeywordButDifferentCase(){
        assertEquals(true, JvmNameUtils.isValidJavaIdentifier("For"));
    }
}
