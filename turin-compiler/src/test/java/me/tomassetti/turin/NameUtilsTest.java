package me.tomassetti.turin;

import org.junit.Test;
import static org.junit.Assert.*;

public class NameUtilsTest {

    @Test
    public void isValidPackageNamePositiveCase(){
        assertEquals(true, NameUtils.isValidPackageName("me.tomassetti.turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceStartingWithDigit(){
        assertEquals(false, NameUtils.isValidPackageName("me.0tomassetti.turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceEmptyInTheMiddle(){
        assertEquals(false, NameUtils.isValidPackageName("me..turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceEmptyAtStart(){
        assertEquals(false, NameUtils.isValidPackageName(".tomassetti.turin"));
    }

    @Test
    public void isValidPackageNameWithAPieceEmptyAtEnd(){
        assertEquals(false, NameUtils.isValidPackageName("me.tomassetti."));
    }

    @Test
    public void isValidJavaIdentifierStartingWithDigit(){
        assertEquals(false, NameUtils.isValidJavaIdentifier("3foo"));
    }

    @Test
    public void isValidJavaIdentifierContainingDollor(){
        assertEquals(true, NameUtils.isValidJavaIdentifier("fo$o"));
    }
}
