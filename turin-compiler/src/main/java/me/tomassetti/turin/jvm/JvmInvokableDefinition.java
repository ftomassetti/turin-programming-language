package me.tomassetti.turin.jvm;

public abstract class JvmInvokableDefinition {
    protected String ownerInternalName;
    protected String name;
    protected String descriptor;

    protected JvmInvokableDefinition(String ownerInternalName, String name, String descriptor) {
        if (!JvmNameUtils.isValidInternalName(ownerInternalName)) {
            throw new IllegalArgumentException(ownerInternalName);
        }
        this.descriptor = descriptor;
        this.name = name;
        this.ownerInternalName = ownerInternalName;
    }

    public String getOwnerInternalName() {
        return ownerInternalName;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
