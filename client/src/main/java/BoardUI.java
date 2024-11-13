import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class BoardUI {
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_HEIGHT = 2;  // Height for each square to make it "square-like"
    private static final String EMPTY_SQUARE = "  ";  // Width of each square
    private static final String LIGHT_COLOR = SET_BG_COLOR_WHITE;
    private static final String DARK_COLOR = SET_BG_COLOR_BLACK;
    private static final String RESET_COLOR = "\u001B[0m";

    private static final String[][] initialBoard = {
            {BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK},
            {BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
            {WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN},
            {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK}
    };

    public static void drawChessBoard(PrintStream out) {
        // Column headers at the top
        out.print("    ");
        for (char col = 'a'; col < 'a' + BOARD_SIZE; col++) {
            out.printf("  %c  ", col);
        }
        out.println();

        // Draw each row of the board with increased square height
        for (int row = 0; row < BOARD_SIZE; row++) {
            // Repeat each square row to create a square appearance
            for (int h = 0; h < SQUARE_HEIGHT; h++) {
                // Add row numbers on the left side only in the middle of the square height
                if (h == SQUARE_HEIGHT / 2) {
                    out.printf(" %d  ", BOARD_SIZE - row);
                } else {
                    out.print("    ");
                }

                // Draw each column in the row
                for (int col = 0; col < BOARD_SIZE; col++) {
                    String color = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;

                    if (h == SQUARE_HEIGHT / 2) {  // Middle row to display piece
                        out.print(color + initialBoard[row][col] + EMPTY_SQUARE + RESET_COLOR);
                    } else {  // Empty padding rows for height
                        out.print(color + "     " + RESET_COLOR);
                    }
                }

                // Add row numbers on the right side only in the middle of the square height
                if (h == SQUARE_HEIGHT / 2) {
                    out.printf("  %d", BOARD_SIZE - row);
                }
                out.println();
            }
        }

        // Column headers at the bottom
        out.print("    ");
        for (char col = 'a'; col < 'a' + BOARD_SIZE; col++) {
            out.printf("  %c  ", col);
        }
        out.println();
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out);
        drawChessBoard(out);
    }
}
