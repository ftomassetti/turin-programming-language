package me.tomassetti.turin.resolvers.compiled;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.List;

public class LoadedFunctionDefinition extends FunctionDefinitionNode {

    private String qualifiedName;

    public LoadedFunctionDefinition(String qualifiedName, TypeUsageNode returnType, List<FormalParameterNode> parameters) {
        super(JvmNameUtils.canonicalToSimple(qualifiedName), returnType, parameters, new BlockStatement(ImmutableList.of()));
        this.qualifiedName = qualifiedName;
    }

    protected String getGeneratedClassQualifiedName() {
        String packagePart = JvmNameUtils.getPackagePart(qualifiedName);
        String simpleName = JvmNameUtils.getSimplePart(qualifiedName);
        String qName = packagePart + "." + CLASS_PREFIX + simpleName;
        if (!JvmNameUtils.isValidQualifiedName(qName)) {
            throw new IllegalStateException(qName);
        }
        return qName;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }
}
