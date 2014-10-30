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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import javax.websocket.Session;
import static org.damcode.web.c4webserver.Utils.printSysOut;

/**
 *
 * @author dm
 */
public class GameController {

    public static final int GAME_WAITING = 1;
    public static final int GAME_ACTIVE = 2;
    public static final int GAME_ENDED = 3;
    public static final int GAME_STARTED = 4;

    private final UUID gameId;
    public Gameboard board;
    public ArrayList<Player> players = new ArrayList<Player>();
    boolean remove = false;
    public int gameStatus;
    public int lastWinnerId;
    public int hostId;

    public GameController() {
        gameId = UUID.randomUUID();
    }

    void addPlayer(Session s) throws IOException {
        s.getBasicRemote().sendText(Utils.assembleChatMessage("Server", "Joined Game!", "server"));

        Player p = new Player(getAvailBoardId(), s);
        players.add(p);
        s.getUserProperties().put("game", this);
    }

    public int getAvailBoardId() {
        int ids = 0;
        for (Player p : players) {
            ids += p.boardId;
        }
        if (ids == 3) {
            return -1;
        } else if (ids == 2 || ids == 0) {
            return 1;
        } else if (ids == 1) {
            return 2;
        }
        return -1;
    }

    UUID getId() {
        return gameId;
    }

    public boolean readyCheck() {
        if (players.size() < 2) {
            return false;
        } else {
            for (Player p : players) {
                if (p.ready == false)
                    return false;
                if (!p.session.isOpen())
                    return false;
            }
        }
        return true;
    }

    public <T> Player getOpponent(T player) {
        Player op = null;

        for (Player p : players) {
            if (player instanceof Session) {
                if (!p.session.equals(player)) {
                    op = p;
                }
            } else if (player instanceof Player) {
                if (!p.equals(player)) {
                    op = p;
                }
            }
        }

        return op;
    }

    public void start() {
        gameStatus = GAME_STARTED;
        Player p = players.get(0);

        if (p.wins > getOpponent(p).wins) {
            p = getOpponent(p);
        }
        printSysOut(p.boardId + ":" + p.wins + "/" + getOpponent(p).boardId + ":" + getOpponent(p).wins);
        board = new Gameboard();
        p.sendDataMessage(MessageBean.MSG_PASS_MOVE_TOKEN, "token");
        p.hasTurn = true;
    }

    public void dropPiece(Session s, int col) throws IOException {
        printSysOut("readycheck: " + readyCheck());
        if (!readyCheck())
            return;

        GameController g = (GameController) s.getUserProperties().get("game");
        Player p = getPlayer(s);

        if (g.getOpponent(s) == null || !g.getOpponent(s).session.isOpen())
            return;

        if (!p.hasTurn)
            return;

        int dropPos = board.dropPiece(p, col);
        if (dropPos != -1) {
            printSysOut("sending move: " + dropPos);
            getOpponent(s).sendDataMessage(MessageBean.MSG_MOVE_ACTION, "" + dropPos);
        }

        if (board.gamedone && board.winner == null) {
            gameStatus = GAME_ENDED;
            printSysOut("NO WINNER" + board.winner.session.getId());

        } else if (board.gamedone && board.winner != null) {
            gameStatus = GAME_ENDED;
            Player winner = board.winner;
            Player loser = getOpponent(board.winner.session);

            printSysOut("WINNER ! " + winner.session.getId());
            winner.wins++;
            winner.sendDataMessage(MessageBean.MSG_WIN, String.valueOf(winner.wins));
            winner.ready = false;

            loser.losses++;
            loser.sendDataMessage(MessageBean.MSG_LOSE, String.valueOf(loser.losses));
            loser.ready = false;

        }

        printSysOut("board: \r\n" + board.toString());
    }

    public Gameboard getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return gameId.toString();
    }

    public Player getPlayer(Session s) {
        for (Player p : players) {
            if (p.session.equals(s)) {
                return p;
            }
        }
        return null;
    }

    void passMoveToken(Session s) throws IOException {
        if (!readyCheck())
            return;
        printSysOut("pass token readycheck: " + readyCheck());
        Player p = getPlayer(s);
        Player opponent = getOpponent(s);

        if (opponent == null || !opponent.session.isOpen())
            return;

        if (p.hasTurn) {
            p.hasTurn = false;
            opponent.sendDataMessage(MessageBean.MSG_PASS_MOVE_TOKEN, "token");
            opponent.hasTurn = true;
        } else {
            System.err.println("HACK: player fake move token: " + p.getName() + " : " + p.session.getId());
            p.sendDataMessage(MessageBean.MSG_GAME_REFRESHBOARD, Arrays.toString(board.getClientBoard(p.boardId)));
        }
    }
}
