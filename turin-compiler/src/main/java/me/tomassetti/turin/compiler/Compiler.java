package me.tomassetti.turin.compiler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.TurinFileWithSource;
import me.tomassetti.turin.parser.analysis.resolvers.*;
import me.tomassetti.turin.parser.analysis.resolvers.jar.JarTypeResolver;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.ast.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.tomassetti.turin.parser.Parser;

public class Compiler {

    private static String VERSION = "0.1 (Crocetta)";

    private Resolver resolver;
    private Options options;

    public Compiler(Resolver resolver, Options options) {
        this.resolver = resolver;
        this.options = options;
    }

    public List<ClassFileDefinition> compile(TurinFile turinFile, ErrorCollector errorCollector) {
        return new Compilation(resolver, errorCollector).compile(turinFile);
    }

    public static class Options {
        public String getDestinationDir() {
            return destinationDir;
        }

        public void setDestinationDir(String destinationDir) {
            this.destinationDir = destinationDir;
        }

        public List<String> getClassPathElements() {
            return classPathElements;
        }

        public void setClassPathElements(List<String> classPathElements) {
            this.classPathElements = classPathElements;
        }

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public boolean isHelp() {
            return help;
        }

        public void setHelp(boolean help) {
            this.help = help;
        }

        public List<String> getSources() {
            return sources;
        }

        public void setSources(List<String> sources) {
            this.sources = sources;
        }

        @Parameter(names = {"-o", "--output"})
        private String destinationDir = "turin_classes";

        @Parameter(names = {"-cp", "--classpath"}, variableArity = true)
        private List<String> classPathElements = new ArrayList<>();

        @Parameter(names = {"-v", "--verbose"})
        private boolean verbose = false;

        @Parameter(names = {"-d", "--debug"})
        private boolean debug = false;

        @Parameter(names = {"-h", "--help"})
        private boolean help = false;

        @Parameter(description = "Files or directories to compile")
        private List<String> sources = new ArrayList<>();
    }

    private static Resolver getResolver(List<String> sources, List<String> classPathElements, List<TurinFile> turinFiles) {
        TypeResolver typeResolver = new ComposedTypeResolver(ImmutableList.<TypeResolver>builder()
                .add(JdkTypeResolver.getInstance())
                .addAll(classPathElements.stream().map((cp) -> toTypeResolver(cp)).collect(Collectors.toList()))
                .build());
        return new ComposedResolver(ImmutableList.of(new InFileResolver(typeResolver), new SrcResolver(turinFiles)));
    }

    private static TypeResolver toTypeResolver(String classPathElement) {
        File file = new File(classPathElement);
        if (file.exists() && file.isFile() && classPathElement.endsWith(".jar")) {
            try {
                return new JarTypeResolver(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException(classPathElement);
        }
    }

    private void compile(File file) throws IOException {
        if (file.isDirectory()) {
            compileDir(file);
        } else {
            compileFile(file);
        }
    }

    private static class ErrorPrinter implements ErrorCollector {

        private String fileDescription;

        public ErrorPrinter(String fileDescription) {
            this.fileDescription = fileDescription;
        }

        @Override
        public void recordSemanticError(Position position, String description) {
            System.err.println(fileDescription + " at " + position + ": (semantic error) " + description);
        }
    }

    private void compileFile(File file) throws IOException {
        TurinFile turinFile = new Parser().parse(new FileInputStream(file));

        for (ClassFileDefinition classFileDefinition : compile(turinFile, new ErrorPrinter(file.getPath()))) {
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
        System.out.println("--------------------------------------------------");
        System.out.println(" Turin Compiler - version " + VERSION);
        System.out.println("--------------------------------------------------\n");

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

        Parser parser = new Parser();

        // First we collect all TurinFiles and we pass it to the resolver
        List<TurinFileWithSource> turinFiles = new ArrayList<>();
        for (String source : options.sources) {
            try {
                turinFiles.addAll(parser.parseAllIn(new File(source)));
            } catch (FileNotFoundException e){
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
                return;
            }
        }
        Resolver resolver = getResolver(options.sources, options.classPathElements, turinFiles.stream().map(TurinFileWithSource::getTurinFile).collect(Collectors.toList()));

        // Then we compile all files
        // TODO consider classpath
        Compiler instance = new Compiler(resolver, options);
        for (TurinFileWithSource turinFile : turinFiles) {
            for (ClassFileDefinition classFileDefinition : instance.compile(turinFile.getTurinFile(), new ErrorPrinter(turinFile.getSource().getPath()))) {
                saveClassFile(classFileDefinition, options);
            }
        }
    }

    private static void saveClassFile(ClassFileDefinition classFileDefinition, Options options) {
        File output = null;
        try {
            output = new File(new File(options.destinationDir).getAbsolutePath() + "/" + classFileDefinition.getName().replaceAll("\\.", "/") + ".class");
            if (options.verbose) {
                System.out.println(" [saving "+output.getPath()+"]");
            }
            output.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(classFileDefinition.getBytecode());
        } catch (IOException e) {
            System.err.println("Problem writing file "+output+": "+ e.getMessage());
            System.exit(3);
        }
    }

}
