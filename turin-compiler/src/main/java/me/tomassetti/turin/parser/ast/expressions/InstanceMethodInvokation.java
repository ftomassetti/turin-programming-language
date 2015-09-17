package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

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
    public TypeUsage calcType(Resolver resolver) {
        List<JvmType> paramTypes = getActualParamValuesInOrder().stream().map((ap)->ap.calcType(resolver).jvmType(resolver)).collect(Collectors.toList());
        return subject.calcType(resolver).returnTypeWhenInvokedWith(methodName, actualParams, resolver, false);
    }

    public JvmMethodDefinition findJvmDefinition(Resolver resolver) {
        List<JvmType> paramTypes = getActualParamValuesInOrder().stream().map((ap)->ap.calcType(resolver).jvmType(resolver)).collect(Collectors.toList());
        return subject.calcType(resolver).findMethodFor(methodName, paramTypes, resolver, false);
    }
}
