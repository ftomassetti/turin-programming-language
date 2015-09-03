package me.tomassetti.turin.compiler.bytecode;

public class JvmFieldDefinition {
    private String declaringType;
    private String name;
    private String typeName;

    public String getDeclaringType() {
        return declaringType;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isStatic() {
        return static_;
    }

    private boolean static_;

    public JvmFieldDefinition(String declaringType, String name, String typeName, boolean static_) {
        this.declaringType = declaringType;
        this.name = name;
        this.typeName = typeName;
        this.static_ = static_;
    }
}
