package me.tomassetti.turin.parser;

import me.tomassetti.turin.parser.ast.TurinFile;

import java.io.File;

/**
 * Represent a pair of a compiled TurinFile and the File from which it was obtained.
 */
public class TurinFileWithSource {
    private File source;
    private TurinFile turinFile;

    public TurinFileWithSource(File source, TurinFile turinFile) {
        this.source = source;
        this.turinFile = turinFile;
    }

    public File getSource() {
        return source;
    }

    public TurinFile getTurinFile() {
        return turinFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TurinFileWithSource that = (TurinFileWithSource) o;

        if (!source.equals(that.source)) return false;
        if (!turinFile.equals(that.turinFile)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + turinFile.hashCode();
        return result;
    }

}
