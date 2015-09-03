package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.InFileResolver;
import me.tomassetti.turin.parser.analysis.JvmConstructorDefinition;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.literals.BooleanLiteral;
import me.tomassetti.turin.parser.ast.literals.FloatLiteral;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TurinTypeDefinitionTest {

    @Test
    public void getQualifiedName() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        turinFile.add(typeDefinition);
        assertEquals("me.tomassetti.MyType", typeDefinition.getQualifiedName());
    }

    @Test
    public void resolveConstructorCallForTypeWithoutPropertiesWhenInvokedWithoutArguments() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        turinFile.add(typeDefinition);

        Resolver resolver = new InFileResolver();

        JvmConstructorDefinition constructor = typeDefinition.resolveConstructorCall(resolver, Collections.emptyList());
        assertEquals("Lme/tomassetti/MyType;", constructor.getJvmType());
        assertEquals("<init>", constructor.getName());
        assertEquals("()V", constructor.getSignature());
    }

    @Test(expected = UnsolvedConstructorException.class)
    public void resolveConstructorCallForTypeWithoutPropertiesWhenInvokedWithTooManyArguments() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        turinFile.add(typeDefinition);

        Resolver resolver = new InFileResolver();

        ActualParam p0 = new ActualParam(new BooleanLiteral(false));
        JvmConstructorDefinition constructor = typeDefinition.resolveConstructorCall(resolver, ImmutableList.of(p0));
    }

    @Test
    public void resolveConstructorCallForTypeWithOnePropertyInvokedWithOneParameterOfCorrectType() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        PropertyDefinition propertyDefinition = new PropertyDefinition("coefficient", PrimitiveTypeUsage.FLOAT);
        typeDefinition.add(propertyDefinition);
        turinFile.add(typeDefinition);

        Resolver resolver = new InFileResolver();

        ActualParam p0 = new ActualParam(new FloatLiteral(0.0f));
        JvmConstructorDefinition constructor = typeDefinition.resolveConstructorCall(resolver, ImmutableList.of(p0));
        assertEquals("Lme/tomassetti/MyType;", constructor.getJvmType());
        assertEquals("<init>", constructor.getName());
        assertEquals("(F)V", constructor.getSignature());
    }

    @Test(expected = UnsolvedConstructorException.class)
    public void resolveConstructorCallForTypeWithOnePropertyInvokedWithOneParameterOfWrongType() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        PropertyDefinition propertyDefinition = new PropertyDefinition("coefficient", PrimitiveTypeUsage.FLOAT);
        typeDefinition.add(propertyDefinition);
        turinFile.add(typeDefinition);

        Resolver resolver = new InFileResolver();

        ActualParam p0 = new ActualParam(new BooleanLiteral(false));
        JvmConstructorDefinition constructor = typeDefinition.resolveConstructorCall(resolver, ImmutableList.of(p0));
    }

    @Test(expected = UnsolvedConstructorException.class)
    public void resolveConstructorCallForTypeWithOnePropertyInvokedWithNamedParameterWithCorrectNameButWrongType() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        PropertyDefinition propertyDefinition = new PropertyDefinition("coefficient", PrimitiveTypeUsage.FLOAT);
        typeDefinition.add(propertyDefinition);
        turinFile.add(typeDefinition);

        Resolver resolver = new InFileResolver();

        ActualParam p0 = new ActualParam("coefficient", new BooleanLiteral(false));
        JvmConstructorDefinition constructor = typeDefinition.resolveConstructorCall(resolver, ImmutableList.of(p0));
        assertEquals("Lme/tomassetti/MyType;", constructor.getJvmType());
        assertEquals("<init>", constructor.getName());
        assertEquals("()V", constructor.getSignature());
    }

    @Test
    public void resolveConstructorCallForTypeWithOnePropertyInvokedWithNamedParameterWithCorrectNameAndCorrectType() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        PropertyDefinition propertyDefinition = new PropertyDefinition("coefficient", PrimitiveTypeUsage.FLOAT);
        typeDefinition.add(propertyDefinition);
        turinFile.add(typeDefinition);

        Resolver resolver = new InFileResolver();

        ActualParam p0 = new ActualParam("coefficient", new FloatLiteral(0.0f));
        JvmConstructorDefinition constructor = typeDefinition.resolveConstructorCall(resolver, ImmutableList.of(p0));
        assertEquals("Lme/tomassetti/MyType;", constructor.getJvmType());
        assertEquals("<init>", constructor.getName());
        assertEquals("(F)V", constructor.getSignature());
    }

    @Test(expected = UnsolvedConstructorException.class)
    public void resolveConstructorCallForTypeWithOnePropertyInvokedWithNamedParameterWithWrongName() {
        TurinFile turinFile = new TurinFile();
        NamespaceDefinition namespace = new NamespaceDefinition("me.tomassetti");
        turinFile.setNameSpace(namespace);
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition("MyType");
        turinFile.add(typeDefinition);

        Resolver resolver = new InFileResolver();

        ActualParam p0 = new ActualParam("zzz", new FloatLiteral(0.0f));
        JvmConstructorDefinition constructor = typeDefinition.resolveConstructorCall(resolver, ImmutableList.of(p0));
    }

}
