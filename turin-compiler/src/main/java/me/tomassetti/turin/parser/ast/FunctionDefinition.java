package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

public class FunctionDefinition extends Invokable {

    public FunctionDefinition(String name, TypeUsage returnType, List<FormalParameter> parameters, Statement body) {
        super(parameters, body, name, returnType);
        this.returnType.parent = this;
        this.parameters.forEach((p) -> p.parent = FunctionDefinition.this );
        this.body.parent = this;
    }

}
