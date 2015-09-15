package me.tomassetti.turin.parser.ast;

public class Point {
    private int line;
    private int column;

    public int getLine() {
        return line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (column != point.column) return false;
        if (line != point.line) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Point{" +
                "line=" + line +
                ", column=" + column +
                '}';
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        return result;
    }

    public Point(int line, int column) {
        this.line = line;
        this.column = column;

    }

    public int getColumn() {
        return column;
    }
}
