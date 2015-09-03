import me.tomassetti.turin.parser.analysis.InFileResolver;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by federico on 29/08/15.
 */
public class ReferenceTypeUsageTest {

    private PropertyReference nameRef;
    private PropertyDefinition ageProperty;

    @Before
    public void setup() {
        // define AST
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        ReferenceTypeUsage intType = new ReferenceTypeUsage("UInt");

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType);

        turinFile.add(nameProperty);

        TurinTypeDefinition mangaCharacter = new TurinTypeDefinition("MangaCharacter");
        ageProperty = new PropertyDefinition("age", intType);
        nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);
    }

    @Test
    public void javaType() {
        Resolver resolver = new InFileResolver();
        assertEquals("Ljava/lang/String;", nameRef.getType(resolver).jvmType(resolver).getSignature());
        assertEquals("I", ageProperty.getType().jvmType(resolver).getSignature());
    }

}
