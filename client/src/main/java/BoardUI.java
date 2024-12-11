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

    public static void drawColumnHeaders(PrintStream out, boolean isBlackPerspective) {
        if (!isBlackPerspective) {
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


    public static String[][] initializeBoard(boolean isWhitePerspective) {
        return isWhitePerspective ? new String[][]{
                // White's perspective board
                {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK},
                {WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN},
                {BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK}
        } : new String[][]{
                // Black's perspective board
                {BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK},
                {BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN},
                {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK}
        };
    }

    public static void drawDynamicChessBoard(PrintStream out, String[][] board, boolean isBlackPerspective) {
        drawColumnHeaders(out, isBlackPerspective);
        for (int row = 0; row < BOARD_SIZE; row++) {
            int displayRow = isBlackPerspective ? BOARD_SIZE - row : row + 1;
            int boardRow = isBlackPerspective ? BOARD_SIZE - row - 1 : row;


            for (int h = 0; h < SQUARE_HEIGHT; h++) {
                if (h == SQUARE_HEIGHT / 2) {
                    out.printf(" %d  ", displayRow);
                } else {
                    out.print("    ");
                }

                for (int col = 0; col < BOARD_SIZE; col++) {
                    int displayCol = isBlackPerspective ? col : BOARD_SIZE - col - 1;
                    String color = (boardRow + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;

                    if (h == SQUARE_HEIGHT / 2) {
                        out.print(color + board[boardRow][displayCol] + EMPTY_SQUARE + RESET_COLOR);
                    } else {
                        out.print(color + "     " + RESET_COLOR);
                    }
                }

                if (h == SQUARE_HEIGHT / 2) {
                    out.printf("  %d", displayRow);
                }
                out.println();
            }
        }

        drawColumnHeaders(out, isBlackPerspective);
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out);

        String[][] boardWhitePerspective = initializeBoard(true); // White's view
        String[][] boardBlackPerspective = initializeBoard(false); // Black's view

        out.println("White Perspective:");
        drawDynamicChessBoard(out, boardWhitePerspective, true);

        out.println("\nBlack Perspective:");
        drawDynamicChessBoard(out, boardBlackPerspective, false);
    }
}
