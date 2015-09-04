package me.tomassetti.turin.compiler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import me.tomassetti.turin.parser.analysis.resolvers.InFileResolver;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import me.tomassetti.turin.parser.Parser;

public class Compiler {

    private static String VERSION = "Alpha 000";

    private Resolver resolver;
    private Options options;

    public Compiler(Resolver resolver, Options options) {
        this.resolver = resolver;
        this.options = options;
    }

    public List<ClassFileDefinition> compile(TurinFile turinFile) {
        return new Compilation(resolver).compile(turinFile);
    }

    public static class Options {

        @Parameter(names = {"-d", "--destination"})
        private String destinationDir = "turin_classes";

        @Parameter(names = {"-cp", "--classpath"}, variableArity = true)
        private List<String> classPathElements = new ArrayList<>();

        @Parameter(names = {"-v", "--verbose"})
        private boolean verbose = false;

        @Parameter(names = {"-h", "--help"})
        private boolean help = false;

        @Parameter(description = "Files or directories to compile")
        private List<String> sources = new ArrayList<>();
    }

    private static Resolver getResolver(List<String> sources, List<String> classPathElements) {
        // TODO use all the elements for resolving
        return new InFileResolver();
    }

    private void compile(File file) throws IOException {
        if (file.isDirectory()) {
            compileDir(file);
        } else {
            compileFile(file);
        }
    }

    private void compileFile(File file) throws IOException {
        TurinFile turinFile = new Parser().parse(new FileInputStream(file));

        for (ClassFileDefinition classFileDefinition : compile(turinFile)) {
            if (options.verbose) {
                System.out.println(" Writing [" + classFileDefinition.getName() + "]");
            }
            File classFile = new File(options.destinationDir +"/" + classFileDefinition.getName().replaceAll("\\.", "/") + ".class");
            classFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(classFile);
            fos.write(classFileDefinition.getBytecode());
            fos.close();
        }
    }

    private void compileDir(File file) throws IOException {
        for (File child : file.listFiles()) {
            compile(child);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("-------------------------------------");
        System.out.println(" Turin Compiler - version " + VERSION);
        System.out.println("-------------------------------------\n");

        Options options = new Options();
        JCommander commander = null;
        try {
            commander = new JCommander(options, args);
        } catch (Throwable t) {
            System.err.println("Problem parsing options: " + t.getMessage());
            System.exit(1);
            return;
        }

        if (options.help) {
            System.out.println("Help demanded - printing usage");
            commander.usage();
            return;
        }

        if (options.sources.isEmpty()) {
            System.err.println("No sources specified");
            commander.usage();
            return;
        }

        Resolver resolver = getResolver(options.sources, options.classPathElements);

        Compiler instance = new Compiler(resolver, options);
        // TODO consider classpath
        for (String source : options.sources) {
            try {
                instance.compile(new File(source));
            } catch (FileNotFoundException e){
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
                return;
            }
        }
    }

}
