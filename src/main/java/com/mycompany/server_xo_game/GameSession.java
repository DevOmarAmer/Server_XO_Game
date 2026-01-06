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

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;
        currentTurn = player1;

        // Fix: Call the method once with both players and the session
        GameSessionManager.addSession(p1, p2, this);
    }

    @Override
    public void run() {
        sendStartMessage();
    }

    private void sendStartMessage() {
        JSONObject start = new JSONObject();
        start.put("type", "game_start");
        start.put("opponent", player2.getUsername());
        player1.sendMessage(start);

        start.put("opponent", player1.getUsername());
        player2.sendMessage(start);
    }

    public synchronized void makeMove(ClientHandler player, int row, int col) {
        if (player != currentTurn || board[row][col] != '\0') {
            return;
        }

        board[row][col] = (player == player1) ? 'X' : 'O';

        // send move to opponent
        ClientHandler opponent = (player == player1) ? player2 : player1;
        JSONObject moveMsg = new JSONObject();
        moveMsg.put("type", "move");
        moveMsg.put("row", row);
        moveMsg.put("col", col);
        moveMsg.put("player", player.getUsername());
        moves.add(moveMsg); // Record the move
        opponent.sendMessage(moveMsg);

        if (checkWin()) {
            endGame(player);
        } else if (isBoardFull()) {
            endGame(); // Draw
        } else {
            currentTurn = opponent;
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

    private boolean checkWin() {
        // check rows, columns, diagonals
        char symbol = (currentTurn == player1) ? 'X' : 'O';
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] == symbol && board[i][1] == symbol && board[i][2] == symbol)
                    || (board[0][i] == symbol && board[1][i] == symbol && board[2][i] == symbol)) {
                return true;
            }
        }
        if ((board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol)
                || (board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol)) {
            return true;
        }
        return false;
    }

    private void endGame(ClientHandler winner) {
        ClientHandler loser = (winner == player1) ? player2 : player1;
        endGame(winner, loser);
    }

    private void endGame(ClientHandler winner, ClientHandler loser) {
        JSONObject msg = new JSONObject();
        msg.put("type", "game_over");
        msg.put("winner", winner.getUsername());
        winner.sendMessage(msg);
        loser.sendMessage(msg);

        // Update scores in DB
        DAO.updateScore(winner.getUsername(), 1);
        DAO.updateScore(loser.getUsername(), 0);

        saveGameRecord(winner.getUsername());

        // Reset status to ONLINE
        winner.setStatus(PlayerStatus.ONLINE);
        loser.setStatus(PlayerStatus.ONLINE);

        // Cleanup session
        GameSessionManager.removeSession(winner);
        GameSessionManager.removeSession(loser);
    }

    private void endGame() { // Draw
        JSONObject msg = new JSONObject();
        msg.put("type", "game_over");
        msg.put("winner", "draw");
        player1.sendMessage(msg);
        player2.sendMessage(msg);

        saveGameRecord("draw");

        // Reset status to ONLINE
        player1.setStatus(PlayerStatus.ONLINE);
        player2.setStatus(PlayerStatus.ONLINE);

        // Cleanup session
        GameSessionManager.removeSession(player1);
        GameSessionManager.removeSession(player2);
    }

    private void saveGameRecord(String result) {
        JSONObject gameRecord = new JSONObject();
        gameRecord.put("player1", player1.getUsername());
        gameRecord.put("player2", player2.getUsername());
        gameRecord.put("result", result);
        gameRecord.put("moves", moves);

        // Assuming DAO.saveGame method exists to store the game record
        DAO.saveGame(gameRecord.toString());
    }
}
