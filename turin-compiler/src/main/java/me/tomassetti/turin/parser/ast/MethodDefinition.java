package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

public class MethodDefinition extends InvokableDefinition {

    public MethodDefinition(String name, TypeUsage returnType, List<FormalParameter> parameters, Statement body) {
        super(parameters, body, name, returnType);
        this.returnType.parent = this;
        this.parameters.forEach((p) -> p.parent = MethodDefinition.this );
        this.body.parent = this;
    }

}
