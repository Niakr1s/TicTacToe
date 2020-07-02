package tictactoe;

public enum Cell {
    X('X'),
    O('O');

    private final char ch;

    private Cell(char ch) {
        this.ch = ch;
    }

    public static Cell fromCh(char ch) {
        for (Cell c : Cell.values()) {
            if (c.ch == ch) {
                return c;
            }
        }
        return null;
    }

    public Cell flipped() {
        if (this == Cell.O) return Cell.X;
        return Cell.O;
    }

    public char getCh() {
        return ch;
    }

    @Override
    public String toString() {
        return "" + ch;
    }
}
