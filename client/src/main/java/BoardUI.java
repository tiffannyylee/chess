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

    public static void drawColumnHeaders(PrintStream out, boolean isWhitePerspective) {
        if (!isWhitePerspective || isWhitePerspective) {
            out.print("    ");
            for (char col = 'a'; col < 'i'; col++) {
                out.printf("  %c  ", col);
            }
            out.println();
        } else {
            out.print("    ");
            for (char col = 'a'; col < 'i'; col--) {
                out.printf("  %c  ", col);
            }
            out.println();
        }

    }

    public static void drawChessBoard2(PrintStream out, String[][] board, boolean isWhitePerspective) {
        drawColumnHeaders(out, isWhitePerspective);

        for (int row = 0; row < BOARD_SIZE; row++) {
            // Flip row index for different perspectives
            int actualRow = isWhitePerspective ? BOARD_SIZE - row - 1 : row;
            int displayRow = isWhitePerspective ? row + 1 : BOARD_SIZE - row;

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
                    int displayCol = isWhitePerspective ? col : BOARD_SIZE - col - 1;
                    String color = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;

                    if (h == SQUARE_HEIGHT / 2) {  // Middle row to display piece
                        out.print(color + board[actualRow][isWhitePerspective ? col : BOARD_SIZE - col - 1] + EMPTY_SQUARE + RESET_COLOR);
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

        drawColumnHeaders(out, isWhitePerspective);
    }


    public static void drawChessBoard(PrintStream out, String[][] board, boolean isWhitePerspective) {
        drawColumnHeaders(out, isWhitePerspective);
        for (int row = 0; row < BOARD_SIZE; row++) {
            int displayRow = BOARD_SIZE - row;
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
                    int displayCol = isWhitePerspective ? col : BOARD_SIZE - col - 1;
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

        drawColumnHeaders(out, isWhitePerspective);
    }

//    public static void drawChessBoardWhite(PrintStream out) {
//        drawChessBoard(out, INITIAL_BOARD_WHITE);
//    }
//
//    public static void drawChessBoardBlack(PrintStream out) {
//        drawChessBoard(out, INITIAL_BOARD_BLACK);
//    }

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

    public static void drawDynamicChessBoard(PrintStream out, String[][] board, boolean isWhitePerspective) {
        drawColumnHeaders(out, isWhitePerspective);
        for (int row = 0; row < BOARD_SIZE; row++) {
            int displayRow = isWhitePerspective ? BOARD_SIZE - row : row + 1;

            for (int h = 0; h < SQUARE_HEIGHT; h++) {
                if (h == SQUARE_HEIGHT / 2) {
                    out.printf(" %d  ", displayRow);
                } else {
                    out.print("    ");
                }

                for (int col = 0; col < BOARD_SIZE; col++) {
                    int displayCol = isWhitePerspective ? col : BOARD_SIZE - col - 1;
                    String color = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;

                    if (h == SQUARE_HEIGHT / 2) {
                        out.print(color + board[row][displayCol] + EMPTY_SQUARE + RESET_COLOR);
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

        drawColumnHeaders(out, isWhitePerspective);
    }

    public static void drawChessBoard3(PrintStream out, String[][] board, boolean isWhitePerspective) {
        drawColumnHeaders(out, isWhitePerspective);

        for (int row = 0; row < BOARD_SIZE; row++) {
            int actualRow = isWhitePerspective ? BOARD_SIZE - row - 1 : row;
            int displayRow = isWhitePerspective ? row + 1 : BOARD_SIZE - row;

            for (int h = 0; h < SQUARE_HEIGHT; h++) {
                if (h == SQUARE_HEIGHT / 2) {
                    out.printf(" %d  ", displayRow);
                } else {
                    out.print("    ");
                }

                for (int col = 0; col < BOARD_SIZE; col++) {
                    String color = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
                    int displayCol = isWhitePerspective ? col : BOARD_SIZE - col - 1;

                    if (h == SQUARE_HEIGHT / 2) {
                        out.print(color + board[actualRow][displayCol] + EMPTY_SQUARE + RESET_COLOR);
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

        drawColumnHeaders(out, isWhitePerspective);
    }


    public static void main(String[] args) {
        var out = new PrintStream(System.out);

        String[][] boardWhitePerspective = initializeBoard(true); // White's view
        String[][] boardBlackPerspective = initializeBoard(false); // Black's view

        out.println("White Perspective:");
        drawChessBoard(out, boardWhitePerspective, true);

        out.println("\nBlack Perspective:");
        drawChessBoard(out, boardBlackPerspective, false);

        out.println("White Perspective:");
        drawDynamicChessBoard(out, boardWhitePerspective, true);

        out.println("\nBlack Perspective:");
        drawDynamicChessBoard(out, boardBlackPerspective, false);
    }
}
