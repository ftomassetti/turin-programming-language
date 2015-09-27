package me.tomassetti.turin.parser.ast;

/**
 * The position of a Node in the source file.
 */
public class Position {

    private Point start;
    private Point end;

    @Override
    public String toString() {
        return "at " + start + " - " + end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (!end.equals(position.end)) return false;
        if (!start.equals(position.start)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public Position(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public static Position create(int startLine, int startCol, int endLine, int endCol) {
        return new Position(new Point(startLine, startCol), new Point(endLine, endCol));
    }

}
