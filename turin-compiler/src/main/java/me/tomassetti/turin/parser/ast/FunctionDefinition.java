package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.annotations.AnnotationUsage;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.FunctionReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FunctionDefinition extends InvokableDefinition implements Named {

    public static final String CLASS_PREFIX = "Function_";
    public static final String INVOKE_METHOD_NAME = "invoke";

    private List<AnnotationUsage> annotations = new ArrayList<>();

    public void addAnnotation(AnnotationUsage annotation) {
        annotation.setParent(this);
        annotations.add(annotation);
    }

    public List<AnnotationUsage> getAnnotations() {
        return annotations;
    }

    @Override
    public Iterable<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        for (Node n : super.getChildren()) {
            children.add(n);
        }
        children.addAll(annotations);
        return children;
    }

    public FunctionDefinition(String name, TypeUsage returnType, List<FormalParameter> parameters, Statement body) {
        super(parameters, body, name, returnType);
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        FunctionReferenceTypeUsage functionReferenceTypeUsage = new FunctionReferenceTypeUsage(parameters.stream().map((fp)->fp.getType()).collect(Collectors.toList()), returnType);
        functionReferenceTypeUsage.setParent(this);
        return functionReferenceTypeUsage;
    }

    @Override
    public Optional<List<FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        return Optional.of(parameters);
    }

    public JvmMethodDefinition jvmMethodDefinition(SymbolResolver resolver) {
        String qName = this.contextName() + "." + CLASS_PREFIX + name;
        String descriptor = "(" + String.join("", parameters.stream().map((fp)->fp.getType().jvmType(resolver).getDescriptor()).collect(Collectors.toList())) + ")" + returnType.jvmType(resolver).getDescriptor();
        return new JvmMethodDefinition(JvmNameUtils.canonicalToInternal(qName), INVOKE_METHOD_NAME, descriptor, true, false);
    }

    public boolean match(List<JvmType> argsTypes, SymbolResolver resolver) {
        if (argsTypes.size() != parameters.size()) {
            return false;
        }
        int i = 0;
        for (FormalParameter formalParameter : parameters) {
            if (!formalParameter.getType().jvmType(resolver).isAssignableBy(argsTypes.get(i))) {
                return false;
            }
            i++;
        }
        return true;
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
