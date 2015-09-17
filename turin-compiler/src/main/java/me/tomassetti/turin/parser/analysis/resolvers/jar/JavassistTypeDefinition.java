package me.tomassetti.turin.parser.analysis.resolvers.jar;

import javassist.CtClass;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

public class JavassistTypeDefinition extends TypeDefinition {

    private CtClass ctClass;

    public JavassistTypeDefinition(CtClass ctClass) {
        super(ctClass.getName());
        this.ctClass = ctClass;
    }

    @Override
    public String getQualifiedName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsage getField(String fieldName, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInterface() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        throw new UnsupportedOperationException();
    }
}
