import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class BoardUI {
    private static final int BOARD_SIZE = 8;
    private static final String EMPTY_SQUARE = "   "; // width of square
    private static final String LIGHT_COLOR = SET_BG_COLOR_LIGHT_GREY; // Light background
    private static final String DARK_COLOR = SET_BG_COLOR_BLUE; // Dark background
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
        //column headers
        out.print("    ");
        for (char col = 'a'; col < 'a' + BOARD_SIZE; col++) {
            out.printf("  %c   ", col);
        }
        out.println();

        // Draw each row of the board
        for (int row = 0; row < BOARD_SIZE; row++) {
            // Row number on the left side
            out.printf(" %d  ", BOARD_SIZE - row);

            for (int col = 0; col < BOARD_SIZE; col++) {
                // Alternate colors for light and dark squares
                String color = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;

                // Print the square with color, piece, and reset color
                out.print(color + initialBoard[row][col] + EMPTY_SQUARE + RESET_COLOR);
            }
            // Row number on the right side
            out.printf("  %d", BOARD_SIZE - row);
            out.println();
        }

        // column headers at the bottom
        out.print("    ");
        for (char col = 'a'; col < 'a' + BOARD_SIZE; col++) {
            out.printf("  %c   ", col);
        }
        out.println();
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out);
        drawChessBoard(out);
    }
}
