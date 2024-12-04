import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import facade.ServerMessageObserver;
import websocket.messages.LoadGame;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.PrintStream;
import java.util.Scanner;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType.*;
import static ui.EscapeSequences.*;


public class Repl implements ServerMessageObserver {
    private final PreLoginClient client;


    public Repl(String serverUrl) {
        client = new PreLoginClient(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to chess! Sign in or register to start.");
        System.out.print(client.help());
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }


    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    //    @Override
//    public void notify(ServerMessage message) {
//        System.out.println(RED + message.toString());
//        printPrompt();
//    }
    @Override
    public void notify(ServerMessage serverMessage) {
        // Based on the type of message, take the appropriate action
        switch (serverMessage.getServerMessageType()) {
            case ServerMessage.ServerMessageType.LOAD_GAME:
                handleLoadGame((LoadGame) serverMessage);
                break;

            case ServerMessage.ServerMessageType.NOTIFICATION:
                handleNotification((Notification) serverMessage);
                break;

            case ServerMessage.ServerMessageType.ERROR:
                handleError((ErrorMessage) serverMessage);
                break;

            // Add more cases for other message types as needed
            default:
                System.err.println("Unknown message type: " + serverMessage.getServerMessageType());
        }
    }

    private String convertPieceToUnicode(ChessPiece piece) {
        switch (piece.getTeamColor()) {
            case WHITE:
                return switch (piece.getPieceType()) {
                    case KING -> "  ♔";
                    case QUEEN -> "  ♕";
                    case ROOK -> "  ♖";
                    case BISHOP -> "  ♗";
                    case KNIGHT -> "  ♘";
                    case PAWN -> "  ♙";
                };
            case BLACK:
                return switch (piece.getPieceType()) {
                    case KING -> "  ♚";
                    case QUEEN -> "  ♛";
                    case ROOK -> "  ♜";
                    case BISHOP -> "  ♝";
                    case KNIGHT -> "  ♞";
                    case PAWN -> "  ♟";
                };
            default:
                return "   "; // In case of an invalid piece
        }
    }

    private String[][] convertBoardToStringArray(ChessBoard board, boolean isWhiteTurn) {
        int size = 8;
        String[][] boardArray = new String[size][size];

        int rowOffset = isWhiteTurn ? 0 : 7; // For White, row 1 is at index 0, for Black, row 8 is at index 0.

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int adjustedRow = isWhiteTurn ? row : 7 - row; // Adjust row based on perspective
                ChessPiece piece = board.getPiece(new ChessPosition(adjustedRow + 1, col + 1));
                if (piece != null) {
                    boardArray[row][col] = convertPieceToUnicode(piece);
                } else {
                    boardArray[row][col] = "   "; // Empty square
                }
            }
        }

        return boardArray;
    }

    // Handle the LOAD_GAME message
    private void handleLoadGame(LoadGame message) {
        var out = new PrintStream(System.out);
        ChessBoard board = message.getGame().game().getBoard();
        String[][] boardArray = convertBoardToStringArray(board, true);
        //boolean isWhitePerspective = message.getGame().game().getCurrentTeamColor() == ChessGame.TeamColor.WHITE;
        //BoardUI.drawDynamicChessBoard(out, boardArray, true);
        BoardUI.drawChessBoard(out, boardArray, false);
        System.out.println("Received Load Game Message: " + message.getGame().game().getBoard());
        // Update the game state with the new game state (e.g., render the board)
        // You might need to update your game UI or internal state here
    }

    // Handle the NOTIFICATION message
    private void handleNotification(Notification notification) {
        System.out.println("Received Notification: " + notification.getMessage());
        // Show the notification to the user, or trigger any other actions
    }

    // Handle the ERROR message
    private void handleError(ErrorMessage errorMessage) {
        System.err.println("Received Error Message: " + errorMessage.getErrorMessage());
        // Display error to the user, handle recovery, or retry logic
    }
}


