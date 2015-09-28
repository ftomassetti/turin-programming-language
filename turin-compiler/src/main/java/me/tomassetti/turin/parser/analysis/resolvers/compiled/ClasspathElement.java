package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import javassist.CtClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

interface ClasspathElement {
    CtClass toCtClass() throws IOException;
    InputStream toInputStream() throws IOException;
    public URL toURL();
}
