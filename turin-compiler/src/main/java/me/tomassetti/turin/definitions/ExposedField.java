package me.tomassetti.turin.definitions;

import me.tomassetti.turin.typesystem.TypeUsage;

/**
 * Something which can be accessed as it was a field.
 * It could be a real field or things like Class.class, array.length, or relations endpoints.
 */
public interface ExposedField {
    boolean accessibleStatically();
    boolean accessibleOnInstance();
    boolean canBeAssigned();
    String name();
    TypeUsage type();
}
