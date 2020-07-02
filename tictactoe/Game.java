package tictactoe;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Game {
    private final IoHelper io = new IoHelper(System.in, System.out);

    public void run() {
        run(3);
    }

    public void run(int dimension) {
        Board board = new Board(io.getDimension());
        io.printBoard(board);
        Board.Status status = board.getStatus();
        while (status == Board.Status.NOT_FINISHED) {
            board = io.moveBoard(board);
            status = board.getStatus();
            io.printBoard(board);
        }
        io.printStatus(status);
    }

    private static class IoHelper {
        private final Scanner in;
        private final PrintStream out;

        IoHelper(InputStream in, PrintStream out) {
            this.in = new Scanner(in);
            this.out = out;
        }

        public Board getBoard() {
            out.print("Enter cells: ");
            String str = in.next();
            try {
                return new Board(str);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input");
                return getBoard();
            }
        }

        public int getDimension() {
            while (true) {
                try {
                    out.println("What dimension do you want?");
                    String line = getNextNonEmptyLine();
                    int dim = Integer.parseInt(line);
                    if (dim < 2) {
                        out.println("Can't be less than 2");
                        continue;
                    }
                    return dim;
                } catch (NumberFormatException e) {
                    out.println("Not a string");
                }
            }
        }

        public void printBoard(Board board) {
            Board.Printer printer = board.new Printer();
            printer.printBoard(out);
        }

        public Board moveBoard(Board board) {
            boolean madeMove = false;
            while (!madeMove) {
                try {
                    out.print("Enter the coordinates: ");
                    String line = getNextNonEmptyLine();
                    board = board.makeMove(line);
                    madeMove = true;
                } catch (Board.CanNotMoveException | IllegalArgumentException e) {
                    out.println(e.getMessage());
                }
            }
            return board;
        }

        private String getNextNonEmptyLine() {
            String res;
            do {
                res = in.nextLine();
            } while (res.isBlank());
            return res;
        }

        public void printStatus(Board.Status status) {
            out.println(status.getStr());
        }
    }

}

