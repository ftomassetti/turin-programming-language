package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.literals.IntLiteral;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.util.Either;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.tomassetti.turin.compiler.ParamUtils.*;

import static org.junit.Assert.*;

import static org.easymock.EasyMock.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TurinTypeDefinition.class)
public class ParamUtilsTest {

    private ActualParam n1 = new ActualParam("n1", new IntLiteral(1));
    private ActualParam n2 = new ActualParam("n2", new IntLiteral(2));
    private ActualParam u1 = new ActualParam(new IntLiteral(3));
    private ActualParam u2 = new ActualParam(new IntLiteral(4));

    private FormalParameter fd1 = new FormalParameter(PrimitiveTypeUsage.INT, "fd1", Optional.of(new IntLiteral(1)));
    private FormalParameter fd2 = new FormalParameter(PrimitiveTypeUsage.INT, "fd2", Optional.of(new IntLiteral(1)));
    private FormalParameter fn1 = new FormalParameter(PrimitiveTypeUsage.INT, "fn1");
    private FormalParameter fn2 = new FormalParameter(PrimitiveTypeUsage.INT, "fn2");

    @Test
    public void verifyOrderOnlyOneNamedParam() {
        assertEquals(true, ParamUtils.verifyOrder(ImmutableList.of(n1)));
    }

    @Test
    public void verifyOrderNamedParamFollowingUnnamedParam() {
        assertEquals(true, verifyOrder(ImmutableList.of(u1, n1)));
    }

    @Test
    public void verifyOrderUnnamedParamFollowingNamedParam() {
        assertEquals(false, verifyOrder(ImmutableList.of(n1, u1)));
    }

    @Test
    public void verifyOrderUnnamedParamBetweenNamedParams() {
        assertEquals(false, verifyOrder(ImmutableList.of(n1, u1, n2)));
    }

    @Test
    public void verifyOrderNamedParamBetweenUnnamedParams() {
        assertEquals(false, verifyOrder(ImmutableList.of(u1, n1, u2)));
    }

    @Test
    public void testUnnamedParams() {
        assertEquals(ImmutableList.of(u1, u2), unnamedParams(ImmutableList.of(u1, n1, u2, n2)));
    }

    @Test
    public void testNamedParams() {
        assertEquals(ImmutableList.of(n1, n2), namedParams(ImmutableList.of(u1, n1, u2, n2)));
    }

    @Test
    public void testhasDefaultParams() {
        assertEquals(false, hasDefaultParams(ImmutableList.of()));
        assertEquals(false, hasDefaultParams(ImmutableList.of(fn1, fn2)));
        assertEquals(true, hasDefaultParams(ImmutableList.of(fn1, fd1, fn2)));
        assertEquals(true, hasDefaultParams(ImmutableList.of(fd1, fn1, fn2)));
        assertEquals(true, hasDefaultParams(ImmutableList.of(fn1, fn2, fd1)));
    }

    @Test
    public void testGetterNameBoolean() {
        assertEquals("isFoo", ParamUtils.getterName(new FormalParameter(PrimitiveTypeUsage.BOOLEAN, "foo")));
        assertEquals("isA", ParamUtils.getterName(new FormalParameter(PrimitiveTypeUsage.BOOLEAN, "a")));
    }

    @Test
    public void testGetterNameNotBoolean() {
        assertEquals("getFoo", ParamUtils.getterName(new FormalParameter(new ReferenceTypeUsage(Boolean.class.getCanonicalName()), "foo")));
        assertEquals("getFoo", ParamUtils.getterName(new FormalParameter(ReferenceTypeUsage.STRING, "foo")));
        assertEquals("getA", ParamUtils.getterName(new FormalParameter(ReferenceTypeUsage.STRING, "a")));
    }

    @Test
    public void testDesugarizeAsteriskParamWhichIsNotAReference() {
        List<FormalParameter> formalParameters = ImmutableList.of(fn1, fn2, fd2, fd2);
        Expression value = createMock(Expression.class);
        SymbolResolver resolver = createMock(SymbolResolver.class);
        TypeUsageNode typeOfAsteriskParam = createMock(TypeUsageNode.class);
        expect(value.calcType(resolver)).andReturn(typeOfAsteriskParam);
        expect(typeOfAsteriskParam.isReference()).andReturn(false);
        Node parent = new IntLiteral(3); // it does not really matter

        replay(value, resolver, typeOfAsteriskParam);
        Either<String, List<ActualParam>> res = desugarizeAsteriskParam(formalParameters, value, resolver, parent);
        assertEquals(true, res.isLeft());
        verify(value, resolver, typeOfAsteriskParam);
    }

    @Test
    public void testDesugarizeAsteriskParamWithAllTheParamsAvailable() {
        List<FormalParameter> formalParameters = ImmutableList.of(fn1, fn2, fd1, fd2);
        Expression value = createMock(Expression.class);
        value.setParent(EasyMock.anyObject());
        value.setParent(EasyMock.anyObject());
        value.setParent(EasyMock.anyObject());
        value.setParent(EasyMock.anyObject());
        SymbolResolver resolver = createMock(SymbolResolver.class);
        ReferenceTypeUsage typeUsageOfAsteriskParam = createMock(ReferenceTypeUsage.class);
        expect(value.calcType(resolver)).andReturn(typeUsageOfAsteriskParam);
        expect(typeUsageOfAsteriskParam.isReference()).andReturn(true);
        expect(typeUsageOfAsteriskParam.asReferenceTypeUsage()).andReturn(typeUsageOfAsteriskParam);
        TypeDefinition typeOfAsteriskParam = PowerMock.createMock(TypeDefinition.class);
        expect(typeUsageOfAsteriskParam.getTypeDefinition(resolver)).andReturn(typeOfAsteriskParam);
        expect(typeOfAsteriskParam.hasMethodFor("getFn1", Collections.emptyList(), resolver, false)).andReturn(true);
        expect(typeOfAsteriskParam.hasMethodFor("getFn2", Collections.emptyList(), resolver, false)).andReturn(true);
        expect(typeOfAsteriskParam.hasMethodFor("getFd1", Collections.emptyList(), resolver, false)).andReturn(true);
        expect(typeOfAsteriskParam.hasMethodFor("getFd2", Collections.emptyList(), resolver, false)).andReturn(true);
        Node parent = new IntLiteral(3); // it does not really matter

        replay(value, resolver, typeUsageOfAsteriskParam);
        PowerMock.replay(typeOfAsteriskParam);
        Either<String, List<ActualParam>> res = desugarizeAsteriskParam(formalParameters, value, resolver, parent);
        assertEquals(true, res.isRight());
        // 2 + the map for default params
        assertEquals(3, res.getRight().size());
        verify(value, resolver, typeUsageOfAsteriskParam);
        PowerMock.verify(typeOfAsteriskParam);
    }

    @Test
    public void testDesugarizeAsteriskParamWithOnlyNonDefaultAvailable() {
        List<FormalParameter> formalParameters = ImmutableList.of(fn1, fn2, fd1, fd2);
        Expression value = createMock(Expression.class);
        value.setParent(EasyMock.anyObject());
        value.setParent(EasyMock.anyObject());
        value.setParent(EasyMock.anyObject());
        value.setParent(EasyMock.anyObject());
        SymbolResolver resolver = createMock(SymbolResolver.class);
        ReferenceTypeUsage typeUsageOfAsteriskParam = createMock(ReferenceTypeUsage.class);
        expect(value.calcType(resolver)).andReturn(typeUsageOfAsteriskParam);
        expect(typeUsageOfAsteriskParam.isReference()).andReturn(true);
        expect(typeUsageOfAsteriskParam.asReferenceTypeUsage()).andReturn(typeUsageOfAsteriskParam);
        TypeDefinition typeOfAsteriskParam = PowerMock.createMock(TypeDefinition.class);
        expect(typeUsageOfAsteriskParam.getTypeDefinition(resolver)).andReturn(typeOfAsteriskParam);
        expect(typeOfAsteriskParam.hasMethodFor("getFn1", Collections.emptyList(), resolver, false)).andReturn(true);
        expect(typeOfAsteriskParam.hasMethodFor("getFn2", Collections.emptyList(), resolver, false)).andReturn(true);
        expect(typeOfAsteriskParam.hasMethodFor("getFd1", Collections.emptyList(), resolver, false)).andReturn(false);
        expect(typeOfAsteriskParam.hasMethodFor("getFd2", Collections.emptyList(), resolver, false)).andReturn(false);
        Node parent = new IntLiteral(3); // it does not really matter

        replay(value, resolver, typeUsageOfAsteriskParam);
        PowerMock.replay(typeOfAsteriskParam);
        Either<String, List<ActualParam>> res = desugarizeAsteriskParam(formalParameters, value, resolver, parent);
        assertEquals(true, res.isRight());
        // 2 + the map for default params
        assertEquals(3, res.getRight().size());
        verify(value, resolver, typeUsageOfAsteriskParam);
        PowerMock.verify(typeOfAsteriskParam);
    }

    @Test
    public void testDesugarizeAsteriskParamWithMissingNonDefault() {
        List<FormalParameter> formalParameters = ImmutableList.of(fn1, fn2, fd1, fd2);
        Expression value = createMock(Expression.class);
        value.setParent(EasyMock.anyObject());
        value.setParent(EasyMock.anyObject());
        SymbolResolver resolver = createMock(SymbolResolver.class);
        ReferenceTypeUsage typeUsageOfAsteriskParam = createMock(ReferenceTypeUsage.class);
        expect(value.calcType(resolver)).andReturn(typeUsageOfAsteriskParam);
        expect(typeUsageOfAsteriskParam.isReference()).andReturn(true);
        expect(typeUsageOfAsteriskParam.asReferenceTypeUsage()).andReturn(typeUsageOfAsteriskParam);
        TypeDefinition typeOfAsteriskParam = PowerMock.createMock(TypeDefinition.class);
        expect(typeUsageOfAsteriskParam.getTypeDefinition(resolver)).andReturn(typeOfAsteriskParam);
        expect(typeOfAsteriskParam.hasMethodFor("getFn1", Collections.emptyList(), resolver, false)).andReturn(true);
        expect(typeOfAsteriskParam.hasMethodFor("getFn2", Collections.emptyList(), resolver, false)).andReturn(false);
        Node parent = new IntLiteral(3); // it does not really matter

        replay(value, resolver, typeUsageOfAsteriskParam);
        PowerMock.replay(typeOfAsteriskParam);
        Either<String, List<ActualParam>> res = desugarizeAsteriskParam(formalParameters, value, resolver, parent);
        assertEquals(true, res.isLeft());
        verify(value, resolver, typeUsageOfAsteriskParam);
        PowerMock.verify(typeOfAsteriskParam);
    }
}
