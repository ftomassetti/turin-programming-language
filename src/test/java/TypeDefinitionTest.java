import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.ast.*;
import me.tomassetti.turin.compiler.ClassFileDefinition;
import me.tomassetti.turin.compiler.Compiler;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by federico on 29/08/15.
 */
public class TypeDefinitionTest {

    private TypeDefinition mangaCharacter;

    @Before
    public void setup() {
        // define AST
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        ReferenceTypeUsage intType = new ReferenceTypeUsage("Int");

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType);

        turinFile.add(nameProperty);

        mangaCharacter = new TypeDefinition("MangaCharacter");
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType);
        PropertyReference nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);
    }

    @Test
    public void getQualifiedName() {
        assertEquals("manga.MangaCharacter", mangaCharacter.getQualifiedName());
    }

}
