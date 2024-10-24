package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.HashSet;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private static final int BOARD_SIZE = 8;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }


    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (getPieceType()) {
            case BISHOP -> bishopMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case KING -> kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece currPiece = board.getPiece(myPosition);
        ChessGame.TeamColor color = currPiece.getTeamColor();
        int direction = (color.equals(ChessGame.TeamColor.WHITE)) ? 1 : -1; // White moves up, Black moves down
        int startRow = myPosition.getRow(); // No need for zero-based indexing

        // Forward move
        ChessPosition forwardPos = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (isValidPosition(forwardPos) && board.getPiece(forwardPos) == null) {
            if (isPromotionRow(forwardPos)) {
                addPromotionMoves(moves, myPosition, forwardPos); // Handles promotion
            } else {
                moves.add(new ChessMove(myPosition, forwardPos, null)); // Regular forward move
            }

            // Two-step move from starting position
            if (isPawnStartingRow(startRow, color)) {
                ChessPosition twoForward = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                if (isValidPosition(twoForward) && board.getPiece(twoForward) == null) {
                    moves.add(new ChessMove(myPosition, twoForward, null)); // Two-step forward move
                }
            }
        }

        // Diagonal captures
        int[] sides = {-1, 1};
        for (int side : sides) {
            ChessPosition diagonal = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + side);
            if (isValidPosition(diagonal)) {
                ChessPiece pieceAtPosition = board.getPiece(diagonal);
                if (pieceAtPosition != null && !pieceAtPosition.getTeamColor().equals(color)) {
                    if (isPromotionRow(diagonal)) {
                        addPromotionMoves(moves, myPosition, diagonal); // Capture with promotion
                    } else {
                        moves.add(new ChessMove(myPosition, diagonal, null)); // Regular capture
                    }
                }
            }
        }

        return moves;
    }

    private boolean isValidPosition(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private boolean isPawnStartingRow(int row, ChessGame.TeamColor color) {
        return (color.equals(ChessGame.TeamColor.WHITE) && row == 2) || (color.equals(ChessGame.TeamColor.BLACK) && row == 7);
    }

    private boolean isPromotionRow(ChessPosition position) {
        return position.getRow() == 1 || position.getRow() == 8;
    }

    private void addPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        for (ChessPiece.PieceType promotionType : new ChessPiece.PieceType[]{
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN}) {
            moves.add(new ChessMove(start, end, promotionType));
        }
    }
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int [][] directions  = {
                {2,1},
                {2,-1},
                {1,2},
                {1,-2},
                {-2,1},
                {-2,-1},
                {-1,2},
                {-1,-2}
        };

        for (int[] direction : directions) {
            //move inside the for loop if it doesnt continue
            int x = myPosition.getRow() -1;
            int y = myPosition.getColumn() -1;
            x += direction[0];
            y += direction[1];
            if (x < 0 || y < 0 || x > 7 || y > 7) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(x + 1, y + 1);
            ChessPiece pieceAtPosition = board.getPiece(newPosition);
            if (pieceAtPosition != null) {
                if (pieceAtPosition.getTeamColor() == pieceColor) {
                    continue;
                }
                moves.add(new ChessMove(myPosition,newPosition,null));
                continue;
            }
            moves.add(new ChessMove(myPosition,newPosition,null));
        }
        return moves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        moves.addAll(bishopMoves(board, myPosition)); // Add Bishop moves
        moves.addAll(rookMoves(board, myPosition));   // Add Rook moves
        return moves;
    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] directions = {
                {1, 1},  // top-right
                {1, -1}, // top-left
                {-1, 1}, // bottom-right
                {-1, -1} // bottom-left
        };

        for (int[] direction : directions) {
            int x = myPosition.getRow() - 1; // converting to 0 based
            int y = myPosition.getColumn() - 1; //converting to 0 based

            while (true) {
                x += direction[0];
                y += direction[1];
                System.out.println("position: " + x + ", " + y);
                //this is zero based (array)
                if (x < 0 || x > 7 || y < 0 || y > 7) {
                    System.out.println("x: " + x + ", y: " + y);
                    break;
                }
                //if there is another piece, and the piece is the same color, add move
                ChessPosition newPosition = new ChessPosition(x + 1, y + 1);
                ChessPiece pieceAtPosition = board.getPiece(newPosition);
                if (pieceAtPosition != null) {
                    if (pieceAtPosition.getTeamColor().equals(getTeamColor())) {
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {1, 0},  // Down
                {-1, 0}, // Up
                {0, 1},  // Right
                {0, -1}  // Left
        };

        // Similar logic as bishopMoves for rook
        for (int[] direction : directions) {
            int x = myPosition.getRow() - 1; // Convert to 0-based
            int y = myPosition.getColumn() - 1; // Convert to 0-based

            while (true) {
                x += direction[0];
                y += direction[1];

                if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                    break; // Out of board bounds
                }

                ChessPosition newPosition = new ChessPosition(x + 1, y + 1); // Convert back to 1-based
                ChessPiece pieceAtPosition = board.getPiece(newPosition);

                if (pieceAtPosition != null) {
                    if (pieceAtPosition.getTeamColor().equals(getTeamColor())) {
                        break; // Blocked by same color piece
                    }
                    moves.add(new ChessMove(myPosition, newPosition, null)); // Capture move
                    break;
                }
                moves.add(new ChessMove(myPosition, newPosition, null)); // Regular move
            }
        }
        return moves;
    }

    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] directions = {
                {1, 1},  // top-right
                {1, -1}, // top-left
                {-1, 1}, // bottom-right
                {-1, -1}, // bottom-left
                {1,0}, //up one
                {-1,0}, //down one
                {0,1}, // one right
                {0,-1} // one left
        };
        for (int[] direction : directions) {
            int x = myPosition.getRow() - 1;
            int y = myPosition.getColumn() - 1;
            x += direction[0];
            y += direction[1];
            if (x < 0 || x > 7 || y < 0 || y > 7) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(x + 1, y + 1);
            ChessPiece pieceAtPosition = board.getPiece(newPosition);
            if (pieceAtPosition != null) {
                if (pieceAtPosition.getTeamColor().equals(getTeamColor())) {
                    System.out.println(pieceAtPosition.getTeamColor());
                    continue;
                }
                moves.add(new ChessMove(myPosition, newPosition, null));
                continue;
            }
            moves.add(new ChessMove(myPosition, newPosition, null));
        }
        return moves;
    }
}


