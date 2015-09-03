package me.tomassetti.turin;

import me.tomassetti.turin.jvm.JvmNameUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class JvmNameUtilsTest {

    @Test
    public void isValidPackageNamePositiveCase(){
        assertEquals(true, JvmNameUtils.isValidPackageName("me.tomassetti.turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceStartingWithDigit(){
        assertEquals(false, JvmNameUtils.isValidPackageName("me.0tomassetti.turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceEmptyInTheMiddle(){
        assertEquals(false, JvmNameUtils.isValidPackageName("me..turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceEmptyAtStart(){
        assertEquals(false, JvmNameUtils.isValidPackageName(".tomassetti.turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceEmptyAtEnd(){
        assertEquals(false, JvmNameUtils.isValidPackageName("me.tomassetti."));
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
