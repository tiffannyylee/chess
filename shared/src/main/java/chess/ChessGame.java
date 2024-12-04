package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessGame.TeamColor currentTeamColor;
    private ChessBoard currentBoard;

    public ChessGame() {
        currentTeamColor = TeamColor.WHITE;
        currentBoard = new ChessBoard();
        currentBoard.initializeDefaultBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeamColor;
    }

    public String getColorString() {
        return currentTeamColor.toString();
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeamColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece piece = currentBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(currentBoard, startPosition);
        for (ChessMove move : moves) {
            ChessBoard originalBoardState = currentBoard.copy();
            simulateMove(startPosition, move.getEndPosition());
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            currentBoard = originalBoardState;
        }
        return validMoves;
    }

    private void simulateMove(ChessPosition moveFrom, ChessPosition moveTo) {
        ChessPiece pieceToMove = currentBoard.getPiece(moveFrom);
        // Move the piece on the board
        if (pieceToMove != null) {
            currentBoard.addPiece(moveTo, pieceToMove);
            currentBoard.addPiece(moveFrom, null);
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        ChessPiece pieceToMove = currentBoard.getPiece(move.getStartPosition());

        if (pieceToMove == null || pieceToMove.getTeamColor() != currentTeamColor) {
            throw new InvalidMoveException();
        }
        for (ChessMove validMove : validMoves) {
            if (move.equals(validMove)) {
                //check if there is a promo piece and replace pawn if there is
                if (move.getPromotionPiece() != null) {
                    pieceToMove = new ChessPiece(pieceToMove.getTeamColor(), move.getPromotionPiece());
                }
                currentBoard.addPiece(validMove.getEndPosition(), pieceToMove);
                currentBoard.addPiece(validMove.getStartPosition(), null);
                setTeamTurn(currentTeamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
                return;
            }
        }
        throw new InvalidMoveException();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPos(teamColor);

        // Iterate over all positions on the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition position = new ChessPosition(row + 1, col + 1);
                ChessPiece pieceAtPos = currentBoard.getPiece(position);

                // Skip empty squares or pieces from the same team
                if (isOpponentPiece(pieceAtPos, teamColor)) {
                    if (canAttackKing(pieceAtPos, position, kingPosition)) {
                        return true; // An opponent's piece can attack the king
                    }
                }
            }
        }
        return false;
    }

    private boolean isOpponentPiece(ChessPiece piece, TeamColor teamColor) {
        return piece != null && piece.getTeamColor() != teamColor;
    }

    private boolean canAttackKing(ChessPiece piece, ChessPosition piecePosition, ChessPosition kingPosition) {
        Collection<ChessMove> possibleMoves = piece.pieceMoves(currentBoard, piecePosition);
        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    private ChessPosition findKingPos(TeamColor team) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition position = new ChessPosition(row + 1, col + 1); //i changed this to plus one bc get piece goes out of bounds
                ChessPiece currPiece = currentBoard.getPiece(position);
                if (currPiece != null && currPiece.getPieceType() == ChessPiece.PieceType.KING && currPiece.getTeamColor() == team) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        // Check if any move can get the team out of check
        for (ChessPosition position : getTeamPiecePositions(teamColor)) {
            ChessPiece pieceAtPos = currentBoard.getPiece(position);
            if (canMakeValidMove(pieceAtPos, position, teamColor)) {
                return false; // A move exists that gets the team out of check
            }
        }
        return true; // No valid move to escape check, it's checkmate
    }

    private List<ChessPosition> getTeamPiecePositions(TeamColor teamColor) {
        List<ChessPosition> positions = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition position = new ChessPosition(row + 1, col + 1);
                ChessPiece piece = currentBoard.getPiece(position);
                if (piece != null && piece.getTeamColor().equals(teamColor)) {
                    positions.add(position);
                }
            }
        }
        return positions;
    }

    private boolean isSafeMove(ChessPosition startPos, ChessPosition endPos, TeamColor teamColor) {
        ChessBoard originalBoardState = currentBoard.copy();
        simulateMove(startPos, endPos);
        boolean isSafe = !isInCheck(teamColor);
        currentBoard = originalBoardState; // Revert to original board state
        return isSafe;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false; // Stalemate can't happen if the team is in check
        }

        // Check if any piece has valid moves that would keep the team out of check
        for (ChessPosition position : getTeamPiecePositions(teamColor)) {
            ChessPiece pieceAtPos = currentBoard.getPiece(position);
            if (canMakeValidMove(pieceAtPos, position, teamColor)) {
                return false; // If any piece can move without putting the team in check, it's not stalemate
            }
        }

        return true; // No valid moves, it's stalemate
    }

    private boolean canMakeValidMove(ChessPiece piece, ChessPosition position, TeamColor teamColor) {
        Collection<ChessMove> validMoves = piece.pieceMoves(currentBoard, position);
        for (ChessMove move : validMoves) {
            if (isSafeMove(position, move.getEndPosition(), teamColor)) {
                return true; // Found a move that doesn't leave the team in check
            }
        }
        return false; // No valid moves found
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.currentBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentTeamColor=" + currentTeamColor +
                ", currentBoard=" + currentBoard +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTeamColor == chessGame.currentTeamColor && Objects.equals(currentBoard, chessGame.currentBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTeamColor, currentBoard);
    }
}
