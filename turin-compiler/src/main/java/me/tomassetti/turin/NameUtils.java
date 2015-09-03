package me.tomassetti.turin;

public final class NameUtils {

    private NameUtils() {
        // prevent instantiation
    }

    public static boolean isValidPackageName(String name){
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (name.isEmpty()) {
            return false;
        }
        // the part would be discarded, we have to check explicitly
        if (name.endsWith(".")){
            return false;
        }
        String[] parts = name.split("\\.");
        for (String part : parts) {
            if (!isValidJavaIdentifier(part)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidJavaIdentifier(String name){
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (name.isEmpty()) {
            return false;
        }
        char firstChar = name.charAt(0);
        if (firstChar != '$' && firstChar != '_' && !Character.isLetter(firstChar)) {
            return false;
        }
        for (int i=0; i<name.length(); i++){
            char c = name.charAt(i);
            if (c != '$' && c != '_' && !Character.isLetter(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

}
