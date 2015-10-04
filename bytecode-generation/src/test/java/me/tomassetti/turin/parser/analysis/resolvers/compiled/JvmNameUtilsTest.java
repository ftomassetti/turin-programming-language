package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import me.tomassetti.jvm.JvmNameUtils;
import org.junit.Assert;
import org.junit.Test;

public class JvmNameUtilsTest {

    @Test
    public void isValidQualifiedNamePositiveCase(){
        Assert.assertEquals(true, JvmNameUtils.isValidQualifiedName("me.tomassetti.turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceStartingWithDigit(){
        Assert.assertEquals(false, JvmNameUtils.isValidQualifiedName("me.0tomassetti.turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceEmptyInTheMiddle(){
        Assert.assertEquals(false, JvmNameUtils.isValidQualifiedName("me..turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceEmptyAtStart(){
        Assert.assertEquals(false, JvmNameUtils.isValidQualifiedName(".tomassetti.turin"));
    }

    @Test
    public void isValidQualifiedNameWithAPieceEmptyAtEnd(){
        Assert.assertEquals(false, JvmNameUtils.isValidQualifiedName("me.tomassetti."));
    }

    @Test
    public void isValidJavaIdentifierStartingWithDigit(){
        Assert.assertEquals(false, JvmNameUtils.isValidJavaIdentifier("3foo"));
    }

    @Test
    public void isValidJavaIdentifierContainingDollar(){
        Assert.assertEquals(true, JvmNameUtils.isValidJavaIdentifier("fo$o"));
    }

    @Test
    public void isValidJavaIdentifierMadeOnlyByDollarsAndUnderscores(){
        Assert.assertEquals(true, JvmNameUtils.isValidJavaIdentifier("_$__$$"));
    }

    @Test
    public void isValidJavaIdentifierContainingPercent(){
        Assert.assertEquals(false, JvmNameUtils.isValidJavaIdentifier("foo%zilla"));
    }

    @Test
    public void isValidJavaIdentifierContainingSpace(){
        Assert.assertEquals(false, JvmNameUtils.isValidJavaIdentifier("foo zilla"));
    }

    @Test
    public void isValidJavaIdentifierCorrespondToKeyword(){
        Assert.assertEquals(false, JvmNameUtils.isValidJavaIdentifier("for"));
    }

    @Test
    public void isValidJavaIdentifierCorrespondToKeywordButDifferentCase(){
        Assert.assertEquals(true, JvmNameUtils.isValidJavaIdentifier("For"));
    }
}
