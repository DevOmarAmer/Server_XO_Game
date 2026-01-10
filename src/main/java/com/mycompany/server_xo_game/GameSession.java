package com.mycompany.server_xo_game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class GameSession implements Runnable {

    private ClientHandler player1;
    private ClientHandler player2;
    private char[][] board = new char[3][3];
    private ClientHandler currentTurn;
    private List<JSONObject> moves = new ArrayList<>();
    private boolean player1WantsRematch = false;
    private boolean player2WantsRematch = false;

    private enum GameState {
        IN_PROGRESS,
        GAME_OVER
    }
    private GameState gameState;

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;
        currentTurn = player1;
        this.gameState = GameState.IN_PROGRESS;
        GameSessionManager.addSession(p1, p2, this);
        initializeBoard();
    }
    
    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '\0';
            }
        }
    }

    @Override
    public void run() {
        sendStartMessage();
    }

    private void sendStartMessage() {
    JSONObject msg1 = new JSONObject();
    msg1.put("type", "game_start");
    msg1.put("opponent", player2.getUsername());
    msg1.put("yourSymbol", "X");
    msg1.put("yourTurn", currentTurn == player1);
    player1.sendMessage(msg1);

    JSONObject msg2 = new JSONObject();
    msg2.put("type", "game_start");
    msg2.put("opponent", player1.getUsername());
    msg2.put("yourSymbol", "O");
    msg2.put("yourTurn", currentTurn == player2);
    player2.sendMessage(msg2);
}


    public synchronized void makeMove(ClientHandler player, int row, int col) {
        // Validate it's the player's turn
        if (player != currentTurn) {
            JSONObject error = new JSONObject();
            error.put("type", "error");
            error.put("message", "Not your turn");
            player.sendMessage(error);
            return;
        }
        
        // Validate move is legal
        if (row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != '\0') {
            JSONObject error = new JSONObject();
            error.put("type", "error");
            error.put("message", "Invalid move");
            player.sendMessage(error);
            return;
        }

        // Make the move
        char symbol = (player == player1) ? 'X' : 'O';
        board[row][col] = symbol;

        // Record the move
        JSONObject moveRecord = new JSONObject();
        moveRecord.put("player", player.getUsername());
        moveRecord.put("row", row);
        moveRecord.put("col", col);
        moveRecord.put("symbol", String.valueOf(symbol));
        moves.add(moveRecord);

        // Send move confirmation to both players
        JSONObject moveMsg = new JSONObject();
        moveMsg.put("type", "move");
        moveMsg.put("row", row);
        moveMsg.put("col", col);
        moveMsg.put("symbol", String.valueOf(symbol));
        moveMsg.put("player", player.getUsername());
        
        player.sendMessage(moveMsg);
        ClientHandler opponent = (player == player1) ? player2 : player1;
        opponent.sendMessage(moveMsg);

        // Check game state
        if (checkWin(symbol)) {
            endGame(player);
        } else if (isBoardFull()) {
            endGame(); // Draw
        } else {
            currentTurn = opponent;
            
            // Send turn notification
            JSONObject turnMsg = new JSONObject();
            turnMsg.put("type", "turn_update");
            turnMsg.put("yourTurn", true);
            opponent.sendMessage(turnMsg);
            
            turnMsg.put("yourTurn", false);
            player.sendMessage(turnMsg);
        }
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '\0') {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkWin(char symbol) {
       
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] == symbol && board[i][1] == symbol && board[i][2] == symbol) ||
                (board[0][i] == symbol && board[1][i] == symbol && board[2][i] == symbol)) {
                return true;
            }
        }
   
        if ((board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol) ||
            (board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol)) {
            return true;
        }
        return false;
    }

    private void endGame(ClientHandler winner) {
        this.gameState = GameState.GAME_OVER;
        ClientHandler loser = (winner == player1) ? player2 : player1;
        
        JSONObject winMsg = new JSONObject();
        winMsg.put("type", "game_over");
        winMsg.put("result", "win");
        winMsg.put("winner", winner.getUsername());
        winner.sendMessage(winMsg);

        JSONObject loseMsg = new JSONObject();
        loseMsg.put("type", "game_over");
        loseMsg.put("result", "lose");
        loseMsg.put("winner", winner.getUsername());
        loser.sendMessage(loseMsg);

        // Update stats in DB
        DAO.updatePlayerStats(winner.getUsername(), "WIN");
        DAO.updatePlayerStats(loser.getUsername(), "LOSS");

        saveGameRecord(winner.getUsername());
    }

    private void endGame() { // Draw
        this.gameState = GameState.GAME_OVER;
        JSONObject drawMsg = new JSONObject();
        drawMsg.put("type", "game_over");
        drawMsg.put("result", "draw");
        drawMsg.put("winner", "draw");
        
        player1.sendMessage(drawMsg);
        player2.sendMessage(drawMsg);

        // Update stats in DB for a draw
        DAO.updatePlayerStats(player1.getUsername(), "DRAW");
        DAO.updatePlayerStats(player2.getUsername(), "DRAW");

        saveGameRecord("draw");
    }

    public synchronized void handlePlayAgainRequest(ClientHandler player) {
        if (gameState != GameState.GAME_OVER) {
            return; // Ignore if game isn't over
        }

        ClientHandler opponent = (player == player1) ? player2 : player1;

        if (player == player1) {
            player1WantsRematch = true;
            // Notify player 2 that player 1 wants to play again
            JSONObject p2Notify = new JSONObject();
            p2Notify.put("type", "opponent_wants_rematch");
            player2.sendMessage(p2Notify);
        } else if (player == player2) {
            player2WantsRematch = true;
            // Notify player 1 that player 2 wants to play again
            JSONObject p1Notify = new JSONObject();
            p1Notify.put("type", "opponent_wants_rematch");
            player1.sendMessage(p1Notify);
        }

        if (player1WantsRematch && player2WantsRematch) {
            startNewGame();
        }
    }
    private void startNewGame() {
    initializeBoard();
    moves.clear();
    
    this.gameState = GameState.IN_PROGRESS;

    player1WantsRematch = false;
    player2WantsRematch = false;

    currentTurn = Math.random() < 0.5 ? player1 : player2;

        sendRematchStart();
    }
    private void sendRematchStart() {

    JSONObject p1 = new JSONObject();
    p1.put("type", "rematch_start");
    p1.put("yourTurn", currentTurn == player1);
    player1.sendMessage(p1);

    JSONObject p2 = new JSONObject();
    p2.put("type", "rematch_start");
    p2.put("yourTurn", currentTurn == player2);
    player2.sendMessage(p2);
}



    
//    private void resetGame() {
//        // Reset board
//        initializeBoard();
//        moves.clear();
//        player1WantsRematch = false;
//        player2WantsRematch = false;
//        
//        // Switch who goes first
//        currentTurn = (currentTurn == player1) ? player2 : player1;
//        
//        // Send new game start messages
//        sendStartMessage();
//    }
public synchronized void handlePlayerQuit(ClientHandler player) {
    // If game is over, the player is just leaving the session
    if (gameState == GameState.GAME_OVER) {
        System.out.println("[GameSession] Player left post-game: " + player.getUsername());
        ClientHandler opponent = (player == player1) ? player2 : player1;
        if (opponent != null) {
            JSONObject msg = new JSONObject();
            msg.put("type", "opponent_left");
            opponent.sendMessage(msg);
        }
        cleanupSession();
        return;
    }
    
    System.out.println("[GameSession] Player quit (forfeit): " + player.getUsername());
    
    ClientHandler opponent = (player == player1) ? player2 : player1;
    
    // Apply forfeit penalties
    DAO.updatePlayerStats(opponent.getUsername(), "WIN");
    DAO.updatePlayerStats(player.getUsername(), "LOSS");

    // Save game record with opponent as winner
    saveGameRecord(opponent.getUsername() + " (Opponent Forfeited)");

    // Notify the quitting player that they lost by forfeit
    JSONObject loseMsg = new JSONObject();
    loseMsg.put("type", "game_over");
    loseMsg.put("result", "lose");
    loseMsg.put("winner", opponent.getUsername());
    loseMsg.put("forfeit", true);
    player.sendMessage(loseMsg);

    // Notify the opponent that they won by forfeit
    JSONObject winMsg = new JSONObject();
    winMsg.put("type", "game_over");
    winMsg.put("result", "win");
    winMsg.put("winner", opponent.getUsername());
    winMsg.put("forfeit", true);
    opponent.sendMessage(winMsg);
    
    cleanupSession();
}

public synchronized void cleanupSession() {
    System.out.println("[GameSession] Cleaning up session (no penalties)");
    
    // Reset both players to ONLINE
    if (player1 != null) {
        player1.setStatus(PlayerStatus.ONLINE);
    }
    if (player2 != null) {
        player2.setStatus(PlayerStatus.ONLINE);
    }

    // Remove session mappings for both players
    GameSessionManager.removeSession(player1);
    GameSessionManager.removeSession(player2);
}


    private void saveGameRecord(String result) {
        JSONObject gameRecord = new JSONObject();
        gameRecord.put("player1", player1.getUsername());
        gameRecord.put("player2", player2.getUsername());
        gameRecord.put("result", result);
        gameRecord.put("moves", moves);

        DAO.saveGame(gameRecord.toString());
    }
}