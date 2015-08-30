package me.tomassetti.turin.parser.ast;

/**
 * Created by federico on 30/08/15.
 */
public class Position {

    private Point start;
    private Point end;

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
}
