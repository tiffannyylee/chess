import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class BoardUI {
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_HEIGHT = 2;
    private static final String EMPTY_SQUARE = "  ";  // Width of each square
    private static final String LIGHT_COLOR = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_COLOR = SET_BG_COLOR_RED;
    private static final String RESET_COLOR = "\u001B[0m";

    private static final String[][] INITIAL_BOARD_WHITE = {
            {BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK},
            {BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN},
            {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK}
    };

    private static final String[][] INITIAL_BOARD_BLACK = {
            {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK},
            {WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN},
            {BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK}
    };

    public static void drawColumnHeaders(PrintStream out, String[][] board) {
        if (board == INITIAL_BOARD_WHITE) {
            out.print("    ");
            for (char col = 'a'; col < 'i'; col++) {
                out.printf("  %c  ", col);
            }
            out.println();
        } else {
            out.print("    ");
            for (char col = 'h'; col >= 'a'; col--) {
                out.printf("  %c  ", col);
            }
            out.println();
        }

    }


    public static void drawChessBoard(PrintStream out, String[][] board) {
        drawColumnHeaders(out, board);
        for (int row = 0; row < BOARD_SIZE; row++) {
            int displayRow = board == INITIAL_BOARD_WHITE ? BOARD_SIZE - row : row + 1;

            // Repeat each square row to create a square
            for (int h = 0; h < SQUARE_HEIGHT; h++) {
                // Add row numbers on the left side only in the middle of the square height
                if (h == SQUARE_HEIGHT / 2) {
                    out.printf(" %d  ", displayRow);
                } else {
                    out.print("    ");
                }

                // Draw each column in the row
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int displayCol = board == INITIAL_BOARD_WHITE ? col : BOARD_SIZE - col - 1;
                    String color = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;

                    if (h == SQUARE_HEIGHT / 2) {  // Middle row to display piece
                        out.print(color + board[row][displayCol] + EMPTY_SQUARE + RESET_COLOR);
                    } else {  // Empty padding rows for height
                        out.print(color + "     " + RESET_COLOR);
                    }
                }

                // Add row numbers on the right side
                if (h == SQUARE_HEIGHT / 2) {
                    out.printf("  %d", displayRow);
                }
                out.println();
            }
        }

        drawColumnHeaders(out, board);
    }

    public static void drawChessBoardWhite(PrintStream out) {
        drawChessBoard(out, INITIAL_BOARD_WHITE);
    }

    public static void drawChessBoardBlack(PrintStream out) {
        drawChessBoard(out, INITIAL_BOARD_BLACK);
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out);
        drawChessBoardBlack(out);
        drawChessBoardWhite(out); //white board
    }
}
