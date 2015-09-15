package me.tomassetti.turin.compiler;

import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.ast.Position;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class AbstractCompilerTest {

    protected static class MyErrorCollector implements ErrorCollector {

        @Override
        public void recordSemanticError(Position position, String description) {
            if (position == null) {
                throw new IllegalArgumentException("null position");
            }
            throw new RuntimeException(position.toString() + " : " + description);
        }
    }

    // Used for debugging
    protected static void saveClassFile(ClassFileDefinition classFileDefinition, String dir) {
        File output = null;
        try {
            output = new File(dir + "/" + classFileDefinition.getName().replaceAll("\\.", "/") + ".class");
            output.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(classFileDefinition.getBytecode());
        } catch (IOException e) {
            System.err.println("Problem writing file "+output+": "+ e.getMessage());
            System.exit(3);
        }
    }

}
