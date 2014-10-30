/*
 * Copyright 2014 dm.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.damcode.web.c4webserver;

import java.util.ArrayList;
import static org.damcode.web.c4webserver.Utils.printSysOut;

/**
 *
 * @author dm
 */
public class Gameboard {

    private int[][] board;
    int moveCounter;
    boolean gamedone = false;
    Player winner;

    public Gameboard() {
        board = new int[7][6];
    }

    // TODO fix player win situation!
    
    public void reset() {
        board = new int[7][6];
        moveCounter = 0;
        gamedone = false;
        winner = null;
    }

    public int dropPiece(Player player, int col) {
        update();
        if (gamedone)
            return -1;
        if (canDrop(col)) {
            moveCounter++;
            printSysOut("moves played: " + moveCounter);
            for (int i = 0; i < board[col].length; i++) {
                if (board[col][i] == 0) {
                    board[col][i] = player.boardId;

                    if (checkConnect(player.boardId, col, i)) {
                        printSysOut("Player " + player + " is winner!");
                        printSysOut("Winning line: " + connectLine);
                        winner = player;
                        gamedone = true;
                    }
                    printSysOut("board drop: " + col + " : " + i);
                    return ((1 + col) * 6) - (i + 1);  // return integer position in straight array of drop.
                }
            }
        }
        return -1;
    }

    private boolean canDrop(int col) {
        for (int i = 0; i < board[col].length; i++) {

            if (board[col][i] == 0)
                return true;
        }

        return false;
    }

    public void update() {
        if (moveCounter >= 7 * 6) {
            printSysOut("no possible moves left!");
            gamedone = true;
            winner = null;
        }
    }

    ArrayList<int[]> connectLine = new ArrayList<int[]>();

    private boolean checkConnect(int player, int col, int row) {
        int connected = 0;

        for (int r = 0; r < board[col].length; r++) {
            if (board[col][r] == player) {
                connected++;
                connectLine.add(new int[]{col, r});
                if (connected == 4)
                    return true;
            } else {
                connectLine.removeAll(connectLine);
                connected = 0;
            }
        }

        connected = 0;

        for (int c = 0; c < board.length; c++) {
            if (board[c][row] == player) {
                connected++;
                connectLine.add(new int[]{c, row});
                if (connected == 4)
                    return true;
            } else {
                connectLine.removeAll(connectLine);
                connected = 0;
            }
        }

        connected = 0;

        int[] cr = getStartDiagBL(col, row);
        int stepCol = cr[0];
        int stepRow = cr[1];

        while (stepCol < 7 && stepRow < 6) {
            if (board[stepCol][stepRow] == player) {
                connected++;
                connectLine.add(new int[]{stepCol, stepRow});
                if (connected == 4)
                    return true;
            } else {
                connectLine.removeAll(connectLine);
                connected = 0;
            }
            stepCol++;
            stepRow++;
        }

        connected = 0;

        cr = getStartDiagTL(col, row);
        stepCol = cr[0];
        stepRow = cr[1];

        while (stepCol < 7 && stepRow >= 0) {
            if (board[stepCol][stepRow] == player) {
                connected++;
                connectLine.add(new int[]{stepCol, stepRow});
                if (connected == 4)
                    return true;
            } else {
                connectLine.removeAll(connectLine);
                connected = 0;
            }
            stepCol++;
            stepRow--;
        }

        connected = 0;

        return false;
    }

    private int[] getStartDiagBL(int col, int row) {
        if (col > row)
            return new int[]{col - row, 0};
        if (col < row)
            return new int[]{0, row - col};

        return new int[]{0, 0};
    }

    private int[] getStartDiagTL(int col, int row) {
        while (col > 0 && row < 5) {
            col--;
            row++;
        }

        return new int[]{col, row};
    }

    @Override
    public String toString() {
        String out = "";
        for (int row = board[0].length - 1; row >= 0; row--) {
            for (int col = 0; col < board.length; col++) {
                out += " - " + board[col][row];
            }
            out += "\r\n";
        }
        return out;
    }

    int[] getClientBoard(int boardId) {
        int b[] = new int[7 * 6];
        int index = 0;
        for (int col = 0; col < board.length; col++) {
            for (int row = board[0].length - 1; row >= 0; row--) {
                if (boardId == 1) {
                    b[index] = board[col][row];
                } else {
                    int p = board[col][row];
                    if (p == 1) {
                        b[index] = 2;
                    } else if (p == 2) {
                        b[index] = 1;
                    }
                }
                index++;
            }
        }
        return b;
    }

}
