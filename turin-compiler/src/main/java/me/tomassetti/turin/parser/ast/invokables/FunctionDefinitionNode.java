package me.tomassetti.turin.parser.ast.invokables;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.Named;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.annotations.AnnotationUsage;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.InvokableReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FunctionDefinitionNode extends InvokableDefinitionNode implements Named, Symbol {

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

    public FunctionDefinitionNode(String name, TypeUsageNode returnType, List<FormalParameterNode> parameters, Statement body) {
        super(parameters, body, name, returnType);
    }

    @Override
    public TypeUsage calcType() {
        InvokableReferenceTypeUsage invokableReferenceTypeUsage = new InvokableReferenceTypeUsage(parameters.stream().map((fp)->fp.getType()).collect(Collectors.toList()), returnType);
        return invokableReferenceTypeUsage;
    }

    @Override
    public Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable) {
        return Optional.of(parameters);
    }

    protected String getGeneratedClassQualifiedName() {
        String qName = this.contextName() + "." + CLASS_PREFIX + name;
        if (!JvmNameUtils.isValidQualifiedName(qName)) {
            throw new IllegalStateException(qName);
        }
        return qName;
    }

    public JvmMethodDefinition jvmMethodDefinition(SymbolResolver resolver) {
        String qName = getGeneratedClassQualifiedName();
        String descriptor = "(" + String.join("", parameters.stream().map((fp)->fp.getType().jvmType().getDescriptor()).collect(Collectors.toList())) + ")" + returnType.jvmType().getDescriptor();
        return new JvmMethodDefinition(JvmNameUtils.canonicalToInternal(qName), INVOKE_METHOD_NAME, descriptor, true, false);
    }

    public boolean match(List<JvmType> argsTypes, SymbolResolver resolver) {
        if (argsTypes.size() != parameters.size()) {
            return false;
        }
        int i = 0;
        for (FormalParameterNode formalParameter : parameters) {
            if (!formalParameter.getType().jvmType().isAssignableBy(argsTypes.get(i))) {
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
