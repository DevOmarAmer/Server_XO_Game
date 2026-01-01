package com.mycompany.server_xo_game;

import org.json.JSONObject;

public class GameSession implements Runnable {
      
    private ClientHandler player1;
    private ClientHandler player2;
    private char[][] board = new char[3][3];
    private ClientHandler currentTurn;

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;
        currentTurn = player1;
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
        if (player != currentTurn || board[row][col] != '\0') return;

        board[row][col] = (player == player1) ? 'X' : 'O';

        // send move to opponent
        ClientHandler opponent = (player == player1) ? player2 : player1;
        JSONObject moveMsg = new JSONObject();
        moveMsg.put("type", "move");
        moveMsg.put("row", row);
        moveMsg.put("col", col);
        moveMsg.put("player", player.getUsername());
        opponent.sendMessage(moveMsg);

        if (checkWin()) {
            endGame(player);
        } else {
            currentTurn = opponent;
        }
    }

    private boolean checkWin() {
        // check rows, columns, diagonals
        char symbol = (currentTurn == player1) ? 'X' : 'O';
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
        ClientHandler loser = (winner == player1) ? player2 : player1;

        JSONObject msg = new JSONObject();
        msg.put("type", "game_over");
        msg.put("winner", winner.getUsername());
        winner.sendMessage(msg);

        msg.put("winner", winner.getUsername());
        loser.sendMessage(msg);

        // Update scores in DB
        DAO.updateScore(winner.getUsername(), 1);
        DAO.updateScore(loser.getUsername(), 0);
    }
}
