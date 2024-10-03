package chess;

import java.util.ArrayList;
import java.util.Collection;
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
        if(piece == null){
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(currentBoard, startPosition);
        for (ChessMove move : moves) {
            ChessBoard originalBoardState = currentBoard.copy();
            simulateMove(startPosition, move.getEndPosition());
            if (!isInCheck(piece.getTeamColor())){
                validMoves.add(move);
            }
            currentBoard = originalBoardState;
        }
        return validMoves;
    }

    private void simulateMove(ChessPosition moveFrom, ChessPosition moveTo) {
        ChessPiece pieceToMove = currentBoard.getPiece(moveFrom);
        // Move the piece on the board
        if(pieceToMove!=null){
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

        if (pieceToMove == null|| pieceToMove.getTeamColor() != currentTeamColor) {
            throw new InvalidMoveException();
        }
        for (ChessMove validMove : validMoves) {
            if (move.equals(validMove)){
                //check if there is a promo piece and replace pawn if there is
                if (move.getPromotionPiece()!=null){
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
        //see if anybody of the other team can move to this position
        for (int row = 0; row<8; row++){
            for (int col = 0; col<8; col++) {
                ChessPosition position = new ChessPosition(row+1, col+1); //i changed this to plus one bc get piece goes out of bounds
                ChessPiece pieceAtPos = currentBoard.getPiece(position);
                if(pieceAtPos!=null&&pieceAtPos.getTeamColor()!=teamColor){
                    Collection<ChessMove> opMoves = pieceAtPos.pieceMoves(currentBoard,position);
                    for(ChessMove move: opMoves){
                        if(move.getEndPosition().equals(kingPosition)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKingPos(TeamColor team){
        for (int row = 0; row<8; row++){
            for (int col = 0; col<8; col++){
                ChessPosition position = new ChessPosition(row+1, col+1); //i changed this to plus one bc get piece goes out of bounds
                ChessPiece currPiece = currentBoard.getPiece(position);
                if (currPiece!=null && currPiece.getPieceType()== ChessPiece.PieceType.KING&&currPiece.getTeamColor()==team){
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
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition position = new ChessPosition(row + 1, col + 1); //i changed this to plus one bc get piece goes out of bounds
                ChessPiece pieceAtPos = currentBoard.getPiece(position);
                if (pieceAtPos != null && pieceAtPos.getTeamColor().equals(teamColor)) {
                    Collection<ChessMove> teamMoves = pieceAtPos.pieceMoves(currentBoard, position);
                    for (ChessMove move : teamMoves) {
                        ChessBoard originalBoardState = currentBoard.copy();
                        simulateMove(position, move.getEndPosition());
                        if (!isInCheck(teamColor)) {
                            currentBoard = originalBoardState;
                            return false;
                        }
                        currentBoard = originalBoardState;
                    }
                }
            }
        }
        return true;
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
            return false;
        }
        boolean hasPieces = false; //check that there are pieces

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition position = new ChessPosition(row + 1, col + 1); // 1-based indexing
                ChessPiece pieceAtPos = currentBoard.getPiece(position);

                if (pieceAtPos != null && pieceAtPos.getTeamColor() == teamColor) {
                    hasPieces = true;
                    Collection<ChessMove> validMoves = pieceAtPos.pieceMoves(currentBoard, position);

                    for (ChessMove move : validMoves) {
                        ChessBoard originalBoardState = currentBoard.copy();
                        simulateMove(position, move.getEndPosition());

                        if (!isInCheck(teamColor)) {
                            currentBoard = originalBoardState;
                            return false;
                        }
                        currentBoard = originalBoardState;
                    }
                }
            }
        }
        return hasPieces;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return currentTeamColor == chessGame.currentTeamColor && Objects.equals(currentBoard, chessGame.currentBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTeamColor, currentBoard);
    }
}
