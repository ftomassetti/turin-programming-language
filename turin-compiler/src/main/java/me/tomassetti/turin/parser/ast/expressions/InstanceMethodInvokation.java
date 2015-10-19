package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.stream.Collectors;

public class InstanceMethodInvokation extends Invokable {

    private Expression subject;
    private String methodName;

    public InstanceMethodInvokation(Expression subject, String methodName, List<ActualParam> params) {
        super(params);
        this.subject = subject;
        this.subject.setParent(this);
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "InstanceMethodInvokation{" +
                "subject=" + subject +
                ", methodName='" + methodName + '\'' +
                '}';
    }

    public Expression getSubject() {
        return subject;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().add(subject).addAll(actualParams).build();
    }

    @Override
    public TypeUsage calcType() {
        List<JvmType> paramTypes = getActualParamValuesInOrder().stream().map((ap)->ap.calcType().jvmType()).collect(Collectors.toList());
        TypeUsage subjectType = subject.calcType();
        return subjectType.returnTypeWhenInvokedWith(methodName, actualParams, false);
    }

    public JvmMethodDefinition findJvmDefinition(SymbolResolver resolver) {
        List<JvmType> paramTypes = getActualParamValuesInOrder().stream().map((ap)->ap.calcType().jvmType()).collect(Collectors.toList());
        return subject.calcType().findMethodFor(methodName, paramTypes, false);
    }

    @Override
    public boolean isOnOverloaded(SymbolResolver resolver) {
        return subject.calcType().getMethod(methodName, false).get().isOverloaded();
    }

    @Override
    protected List<? extends FormalParameter> formalParameters(SymbolResolver resolver) {
        TypeUsage typeUsage = subject.calcType();
        if (!typeUsage.isReference()) {
            throw new UnsupportedOperationException();
        }
        return typeUsage.asReferenceTypeUsage().getTypeDefinition().getMethodParams(methodName, actualParams, false);
    }
}
