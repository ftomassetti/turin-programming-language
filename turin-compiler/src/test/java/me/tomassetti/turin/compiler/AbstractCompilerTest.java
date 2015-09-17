package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.*;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.ast.Position;
import me.tomassetti.turin.parser.ast.TurinFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

    protected Resolver getResolverFor(TurinFile turinFile) {
        return new ComposedResolver(ImmutableList.of(new InFileResolver(JdkTypeResolver.getInstance()), new SrcResolver(ImmutableList.of(turinFile))));
    }

    public Method compileFunction(String exampleName, Class[] paramTypes) throws NoSuchMethodException, IOException {
        return compileFunction(exampleName, paramTypes, Collections.emptyList());
    }

    public Method compileFunction(String exampleName, Class[] paramTypes, List<String> classPathElements) throws NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/" + exampleName + ".to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        options.setClassPathElements(classPathElements);
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        saveClassFile(classFileDefinitions.get(0), "tmp");
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class functionClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(0, functionClass.getConstructors().length);

        Method invoke = functionClass.getMethod("invoke", paramTypes);
        return invoke;
    }

}
