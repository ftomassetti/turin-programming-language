package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.InternalInvokableDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.Invokable;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstanceMethodInvokation extends InvokableExpr {

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
        Invokable invokableType = subjectType.getMethod(methodName, false).get();
        InternalInvokableDefinition internalInvokableDefinition = invokableType.internalInvokableDefinitionFor(actualParams).get();
        return internalInvokableDefinition.asMethod().getReturnType();
    }

    public JvmMethodDefinition findJvmDefinition(SymbolResolver resolver) {
        List<ActualParam> paramTypes = getActualParamValuesInOrder().stream().map((e) -> {
            ActualParam ap = new ActualParam (e);
            ap.setParent(InstanceMethodInvokation.this);
            return ap;
        }).collect(Collectors.toList());
        TypeUsage subjectType = subject.calcType();
        Optional<Invokable> method = subjectType.getMethod(methodName, false);
        Optional<? extends InternalInvokableDefinition> invokableDefinition = method.get().internalInvokableDefinitionFor(paramTypes);
        if (!invokableDefinition.isPresent()) {
            throw new IllegalStateException("Unable to retrieve the JVM definition: " + method.get().getClass().getCanonicalName());
        }
        return invokableDefinition.get().asMethod().getJvmMethodDefinition();
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
