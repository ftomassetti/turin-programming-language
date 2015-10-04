package me.tomassetti.jvm;

public class JvmFieldDefinition {
    private String ownerInternalName;
    private String fieldName;
    private String descriptor;

    public String getOwnerInternalName() {
        return ownerInternalName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isStatic() {
        return static_;
    }

    private boolean static_;

    public JvmFieldDefinition(String ownerInternalName, String fieldName, String descriptor, boolean static_) {
        this.ownerInternalName = ownerInternalName;
        this.fieldName = fieldName;
        this.descriptor = descriptor;
        this.static_ = static_;
    }
}
