package me.tomassetti.turin.parser.ast;

/**
 * Created by federico on 30/08/15.
 */
public class Point {
    private int line;
    private int column;

    public int getLine() {
        return line;
    }

    public Point(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getColumn() {
        return column;
    }
}
