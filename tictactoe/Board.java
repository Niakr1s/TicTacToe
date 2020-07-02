package tictactoe;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Objects;

public class Board {
    /**
     * Cell[row][col]
     */
    private final Cell[][] field;
    private Cell current = Cell.X;

    /**
     * Constructs Board with default dimension = 3;
     */
    public Board() {
        this(3);
    }

    /**
     * @param cellsStr is string of type "OOOXXX___"
     */
    public Board(String cellsStr) throws IllegalArgumentException {
        int dim = (int) Math.sqrt(cellsStr.length());
        if (dim * dim != cellsStr.length()) throw new IllegalArgumentException();

        // I can't do
        field = new Cell[dim][dim];

        char[] chars = cellsStr.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Cell cell = Cell.fromCh(chars[i]);
            setCell(i, cell);
        }
    }

    /**
     * Constructs Board of dimension;
     */
    public Board(int dimension) {
        field = new Cell[dimension][dimension];
    }

    public Board(Board other) {
        field = other.field.clone();
        current = other.current;
    }

    /**
     * @param str contains row and col in human-readable format, ie: col and row are in range [1,..dimension],
     *            col starting from left, row starting from bottom
     * @return copy of new board
     */
    public Board makeMove(String str) throws IllegalArgumentException, CanNotMoveException {
        Parser p = new Parser();
        Pos pos = p.parseMoveStr(str);
        return makeMove(pos.row, pos.col);
    }

    public static class CanNotMoveException extends Exception {
        CanNotMoveException(String str) {
            super(str);
        }
    }

    private Board makeMove(int row, int col) throws CanNotMoveException {
        Cell cell = getCell(row, col);
        if (cell != null) throw new CanNotMoveException("Target Cell isn't empty");
        Board res = new Board(this);
        res.setCell(row, col, current);
        res.flip();
        return res;
    }

    public Status getStatus() {
        return new StatusChecker().getStatus();
    }

    private void flip() {
        this.current = this.current.flipped();
    }

    private void setCell(int row, int col, Cell cell) {
        field[row][col] = cell;
    }

    private void setCell(int pos, Cell cell) {
        int row = pos / dimension();
        int col = pos % dimension();
        setCell(row, col, cell);
    }

    private Cell getCell(int row, int col) {
        return field[row][col];
    }

    private Cell getCell(int pos) {
        int row = pos / dimension();
        int col = pos % dimension();
        return getCell(row, col);
    }

    private void removeCell(int row, int col) {
        setCell(row, col, null);
    }

    public int size() {
        int sz = 0;
        for (Cell[] cells : field) {
            sz += cells.length;
        }
        return sz;
    }

    public int dimension() {
        return field.length;
    }

    private interface CellArrayProcessor {
        void process(Cell[] cells);
    }

    private interface CellProcessor {
        void process(Cell cell);
    }

    private static class MutWrapper<T> {
        public T wrapped;

        public MutWrapper(T toWrap) {
            wrapped = toWrap;
        }
    }

    private class StatusChecker {
        private Status getStatus() {
            if (isImpossible()) {
                return Status.IMPOSSIBLE;
            }
            if (getWinRowsAmount(Cell.X) > 0) {
                return Status.X_WINS;
            }
            if (getWinRowsAmount(Cell.O) > 0) {
                return Status.Y_WINS;
            }
            if (hasEmptyCells()) {
                return Status.NOT_FINISHED;
            }
            return Status.DRAW;
        }

        private boolean isImpossible() {
            if (Math.abs(numOfX() - numOfO()) >= 2) {
                return true;
            }
            return getWinRowsAmount(Cell.X) > 0 && getWinRowsAmount(Cell.O) > 0;
        }

        private int getWinRowsAmount(Cell of) {
            MutWrapper<Integer> res = new MutWrapper<>(0);
            CellArrayProcessor f = cells -> {
                for (Cell c : cells) {
                    if (c != of) return;
                }
                res.wrapped++;
            };
            forEachRow(f);
            return res.wrapped;
        }

        private int numOf(Cell of) {
            MutWrapper<Integer> num = new MutWrapper<>(0);
            forEach(cell -> {
                if (cell == of) num.wrapped++;
            });
            return num.wrapped;
        }

        private int numOfX() {
            return numOf(Cell.X);
        }

        private int numOfO() {
            return numOf(Cell.O);
        }

        private void forEach(CellProcessor f) {
            for (Cell[] cells : field) {
                for (Cell cell : cells) {
                    f.process(cell);
                }
            }
        }

        private void forEachRow(CellArrayProcessor f) {
            LinesIterator iterator = new LinesIterator();
            while (iterator.hasNext()) {
                Cell[] row = iterator.next();
                f.process(row);
            }
        }

        private boolean hasEmptyCells() {
            for (int i = 0; i < size(); i++) {
                if (getCell(i) == null) return true;
            }
            return false;
        }
    }

    public class Printer {
        public void printBoard(PrintStream out) {
            printDelimiter(out);
            for (int row = 0; row < field.length; row++) {
                printRow(row, out);
            }
            printDelimiter(out);
        }

        private void printDelimiter(PrintStream out) {
            int len = dimension() * 2 + 3;
            out.println("-".repeat(len));
        }

        private void printRow(int row, PrintStream out) {
            out.print("| ");
            for (int col = 0; col < field[row].length; col++) {
                Cell cell = field[row][col];

                if (Objects.isNull(cell)) out.print("_");
                else out.print(cell.getCh());

                out.print(" ");
            }
            out.println("|");
        }
    }

    private class Parser {
        /**
         * @param str of a human-readable format (col, row)
         */
        private Pos parseMoveStr(String str) throws IllegalArgumentException {
            String[] splitted = str.split("\\s+");
            if (splitted.length != 2) throw new IllegalArgumentException("Length should be 2");
            int col, row;
            try {
                col = Integer.parseInt(splitted[0]);
                row = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Should contain only numbers");
            }
            // converting to inner coordinates
            col--;
            row--;
            if (col < 0 || col >= dimension() || row < 0 || row >= dimension()) {
                throw new IllegalArgumentException(String.format("Coordinates should be from 1 to %d", dimension()));
            }
            row = dimension() - row - 1;
            return new Pos(row, col);
        }
    }

    public class LinesIterator implements Iterator<Cell[]> {
        private final Iterator<Cell[]>[] iterators;

        public LinesIterator() {
            this.iterators = new Iterator[]{
                    new ColIterator(),
                    new RowIterator(),
                    new DiagonalIterator()
            };
        }

        @Override
        public Cell[] next() {
            for (Iterator<Cell[]> iterator: iterators) {
                if (iterator.hasNext()) {
                    return iterator.next();
                }
            }
            return new Cell[0];
        }

        @Override
        public boolean hasNext() {
            for (Iterator<Cell[]> iterator: iterators) {
                if (iterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        private class RowIterator implements Iterator<Cell[]> {
            private int rowIdx = 0;

            public boolean hasNext() {
                return rowIdx < dimension();
            }

            public Cell[] next() {
                return field[rowIdx++].clone();
            }
        }

        private class ColIterator implements Iterator<Cell[]> {
            private int colIdx = 0;

            @Override
            public boolean hasNext() {
                return colIdx < dimension();
            }

            @Override
            public Cell[] next() {
                Cell[] res = new Cell[field.length];
                for (int row = 0; row < field.length; row++) {
                    res[row] = field[row][colIdx];
                }
                colIdx++;
                return res;
            }
        }

        private class DiagonalIterator implements Iterator<Cell[]> {
            private int idx = 0;
            private final Cell[][] diagonals = new Cell[2][dimension()];

            DiagonalIterator() {
                for (int i = 0; i < dimension(); i++) {
                    diagonals[0][i] = field[i][i];
                    diagonals[1][i] = field[i][field[i].length - i - 1];
                }
            }

            @Override
            public boolean hasNext() {
                return idx < diagonals.length;
            }

            @Override
            public Cell[] next() {
                return diagonals[idx++];
            }
        }
    }

    private static class Pos {
        public final int row;
        public final int col;

        Pos(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public enum  Status {
        NOT_FINISHED("Game not finished"),
        DRAW("Draw"),
        X_WINS("X wins"),
        Y_WINS("O wins"),
        IMPOSSIBLE("Impossible");

        private String str;

        Status(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }
}

