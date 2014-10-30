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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import static org.damcode.web.c4webserver.Utils.printSysOut;
import org.json.JSONObject;

/**
 *
 * @author dm
 */
@ServerEndpoint(value = "/{id}/c4", decoders = MessageDecoder.class)
public class Server {

    static final boolean DEBUG = false;
    static final List<GameController> activeGames = Collections.synchronizedList(new ArrayList<GameController>());
    static final List<GameController> waitingGames = Collections.synchronizedList(new ArrayList<GameController>());

    @OnOpen
    public void onOpen(Session session, @PathParam("id") String gameId) throws IOException {
        printSysOut("new session: " + session.getId() + ", game id: " + gameId + "from ip: ");
        session.getUserProperties().put("gameid", gameId);
        session.getUserProperties().put("lobby", true);

        String connectedPlayers = "";
        int pCount = 0;
        for (Session s : session.getOpenSessions()) {
            if (!s.equals(session)) {
                String name = (String) s.getUserProperties().get("name");
                connectedPlayers += name + ", ";
                pCount++;
            }
        }
        if (pCount > 0) {
            printSysOut("Connected Players: " + connectedPlayers);
            session.getBasicRemote().sendText(Utils.assembleChatMessage("Server", connectedPlayers, "other players"));
        }

        for (GameController gc : waitingGames) {
            sendGameAvailMessage(session, gc.getId().toString(), gc.players.get(0));
        }
    }

    @OnClose
    public void onClose(Session s) {
        printSysOut("Closing session: " + s.getId());

        GameController g = getGame(s);
        if (g != null) {

            Player p = g.getPlayer(s);
            if (g.gameStatus == GameController.GAME_WAITING) {
                waitingGames.remove(g);
                printSysOut("waiting game removed");
            }

            if (g.gameStatus == GameController.GAME_ACTIVE
                    || g.gameStatus == GameController.GAME_STARTED
                    || g.gameStatus == GameController.GAME_ENDED) {

                if (g.players.size() < 2) {
                    activeGames.remove(g);
                    printSysOut("active game removed");
                } else {
                    quitGame(s, MessageBean.MSG_PLAYER_DISC);
                    printSysOut("Quitting session, telling opponent");
                }

            }
        }
    }

    @OnMessage
    public void onMessage(MessageBean message, Session session, @PathParam("id") String gameId) {
        printSysOut("Current active games: " + activeGames.size());
        printSysOut("Current waiting games: " + waitingGames.size());
        String players = "";
        int pCount = 0;
        for (Session s : session.getOpenSessions()) {
            if (!s.equals(session)) {
                String name = (String) s.getUserProperties().get("name");
                players += name;
                pCount++;
            }
        }
        printSysOut("Connected Players: " + players + " / " + pCount);

        try {

            processMessage(message, session);

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendGameAvailMessage(Session s, String gameId, Player host) {
        try {
            JSONObject obj = new JSONObject()
                    .put("cmd", MessageBean.MSG_GAME_AVAILABLE)
                    .put("data", new JSONObject()
                            .put("id", gameId)
                            .put("host", host.getName()));

            s.getBasicRemote().sendText(obj.toString());
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void processMessage(MessageBean m, Session s) throws IOException {
        Player p, o;
        GameController g;

        switch (m.getCommand()) {

            case MessageBean.MSG_CHAT:
                doChatMessage(m, s);
                break;

            case MessageBean.MSG_PASS_MOVE_TOKEN:
                getGame(s).passMoveToken(s);
                break;

            case MessageBean.MSG_MOVE_ACTION:
                int c = Integer.parseInt(m.getData());
                getGame(s).dropPiece(s, c);
                printSysOut("player move: " + m.getData());
                break;

            case MessageBean.MSG_START_GAME:
                if (isAlreadyPlaying(s)) {
                    break;
                }
                GameController game = new GameController();
                game.addPlayer(s);
                s.getUserProperties().put("gameid", game.getId());
                waitingGames.add(game);
                game.gameStatus = GameController.GAME_WAITING;

                s.getBasicRemote().sendText(
                        Utils.assembleChatMessage("Server",
                                "ok, started game, waiting for players to join.", "server"));

                game.getPlayer(s).sendDataMessage(MessageBean.MSG_START_GAME, game.getId().toString());
                //sendDataMessage(s, , );
                printSysOut("start game: " + m.getData());
                break;

            case MessageBean.MSG_PLAYER_NAME:
                String name = m.getData();
                s.getUserProperties().put("name", name);
                printSysOut("user with name: " + name);
                for (Session ss : s.getOpenSessions()) {
                    if (ss.equals(s))
                        continue;
                    ss.getBasicRemote().sendText(
                            Utils.assembleChatMessage("Server", "Player join lobby: " + name, "server"));
                }
                break;

            case MessageBean.MSG_JOIN_GAME:
                if (isAlreadyPlaying(s)) {
                    break;
                }

                printSysOut("joingame: " + m.getData());
                g = joinGame(s, m);
                if (g != null) {
                    try {
                        waitingGames.remove(g);
                    } catch (Exception e) {
                        printSysOut("JOIN FAILED");
                    }
                } else {
                    printSysOut("NO GAME MATCH FOUND: " + m.getData());
                    Utils.sendDataMessage(MessageBean.MSG_PLAYER_QUIT, "Game has expired!", s);
                }
                break;

            case MessageBean.MSG_JOIN_LOBBY_CHAT:
                s.getUserProperties().put("lobby", Boolean.parseBoolean(m.getData()));
                break;

            case MessageBean.MSG_GAME_AVAILABLE:
                if (isAlreadyPlaying(s)) {
                    break;
                }
                synchronized (waitingGames) {
                    if (waitingGames.isEmpty()) {
                        s.getBasicRemote().sendText(Utils.assembleChatMessage("Server", "No games available, try starting one!", "server"));
                    }
                    for (GameController gc : waitingGames) {
                        sendGameAvailMessage(s, gc.getId().toString(), gc.players.get(0));
                    }
                }
                break;

            case MessageBean.MSG_PLAYER_READY:
                g = getGame(s);
                if (g == null || g.gameStatus == GameController.GAME_STARTED)
                    break;
                printSysOut("gamestate = " + g.gameStatus);
                p = g.getPlayer(s);
                o = g.getOpponent(s);

                p.ready = true;

                if (g.readyCheck()) {
                    p.sendDataMessage(MessageBean.MSG_PLAYER_READY, "rdy");
                    o.sendDataMessage(MessageBean.MSG_PLAYER_READY, "rdy");
                    g.start();
                }
                break;

            case MessageBean.MSG_PLAYER_QUIT:
                quitGame(s, MessageBean.MSG_PLAYER_QUIT);
                break;

            case MessageBean.MSG_PLAYER_SELECT:
                g = getGame(s);
                if (g == null)
                    break;
                p = g.getPlayer(s);
                p.imageId = Integer.parseInt(m.getData());
                o = g.getOpponent(s);
                if (o != null) {
                    printSysOut("SENT DISABLE MESSAGE: " + p.imageId + " to: " + o.session.getId());
                    o.sendDataMessage(MessageBean.MSG_PLAYER_SELECT, m.getData());
                }
                break;

            default:
                break;

        }

    }

    private GameController getGame(Session s) {
        return (GameController) s.getUserProperties().get("game");
    }

    @OnError
    public void onError(Throwable t) {
        printSysOut("wtf error: " + t.getMessage() + Arrays.toString(t.getStackTrace()));
        t.printStackTrace();
    }

    private synchronized void doChatMessage(MessageBean message, Session session) {
        String name = (String) session.getUserProperties().get("name");
        if (name == null) {
            name = "unknown";
        }

        String data = message.getData().replace("\\", "");
        data = data.replace("\"", "\\\"");

        for (Session s : session.getOpenSessions()) {
            try {
                if (s == session)
                    continue;

                if (s.getUserProperties().get("gameid").equals(session.getUserProperties().get("gameid"))) {   // only send messages to people in same "game"
                    s.getBasicRemote().sendText(Utils.assembleChatMessage(name, data));

                } else if ((Boolean) s.getUserProperties().get("lobby") == true && (Boolean) session.getUserProperties().get("lobby") == true) {
                    if (!session.getUserProperties().get("gameid").equals("lobby")) {
                        s.getBasicRemote().sendText(Utils.assembleChatMessage(name, data, "playing"));
                    } else {
                        s.getBasicRemote().sendText(Utils.assembleChatMessage(name, data, "lobby"));
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private GameController joinGame(Session s, MessageBean m) throws IOException {
        synchronized (waitingGames) {
            for (GameController g : waitingGames) {
                if (g.getId().equals(UUID.fromString(m.getData()))) {
                    printSysOut("found game match");
                    g.addPlayer(s);
                    s.getUserProperties().put("gameid", g.getId());
                    activeGames.add(g);
                    g.gameStatus = GameController.GAME_ACTIVE;

                    printSysOut("goplayer: " + g.getOpponent(s).getName());
                    g.getPlayer(s).sendDataMessage(MessageBean.MSG_PLAYER_SELECT, String.valueOf(g.getOpponent(s).imageId));
                    printSysOut("send disable image id: " + String.valueOf(g.getOpponent(s).imageId));

                    return g;
                } else {
                    printSysOut("NO GAME MATCH FOUND: " + m.getData());
                }
            }
        }
        return null;
    }

    private boolean isAlreadyPlaying(Session s) throws IOException {
        GameController g = getGame(s);
        if (g != null) {
            if (g.gameStatus == GameController.GAME_ACTIVE) {
                s.getBasicRemote().sendText(
                        Utils.assembleChatMessage("Server", "Error, already playing game", "server"));
                return true;
            } else if (g.gameStatus == GameController.GAME_WAITING) {
                s.getBasicRemote().sendText(
                        Utils.assembleChatMessage("Server", "Error, already waiting for game", "server"));
                return true;

            
//            } else if (g.gameStatus == GameController.GAME_STARTED) {
//
            } else if (g.gameStatus == GameController.GAME_ENDED) {
                s.getBasicRemote().sendText(
                        Utils.assembleChatMessage("Server", "Please quit current match first", "server"));
                return true;

            }
        }

        return false;
    }

    private void quitGame(Session s, int reason) {
        printSysOut("got quitgame from : " + s.getId());
        GameController g = getGame(s);

        // TODO : fix quitgame to destroy any games attached when players quit!
        if (g == null) {
            printSysOut("quitgame game was null");
            return;
        }

        Player p = g.getPlayer(s);
        Player opponent = g.getOpponent(s);

        if (opponent != null) {
            // could still retrieve game if player reconnects
            // destroy only the player object from the game;
            // TODO implement this later, for now just close the game object

            opponent.sendDataMessage(reason, "quit");
            printSysOut("Quitting player removed: " + (getGame(s).players.remove(p)));

        } else {        // no opponent, shutdown game;
            if (g.gameStatus == GameController.GAME_ACTIVE || g.gameStatus == GameController.GAME_STARTED || g.gameStatus == GameController.GAME_ENDED) {
                printSysOut("Last player removed: " + (getGame(s).players.remove(p)));
                printSysOut("quitgame: removed active game");
                activeGames.remove(g);

            } else if (g.gameStatus == GameController.GAME_WAITING) {
                printSysOut("Last waiting player removed: " + (getGame(s).players.remove(p)));
                printSysOut("quitgame: removed waiting game");
                waitingGames.remove(g);
            }
        }

        printSysOut("players size: " + g.players.size());
        s.getUserProperties().remove("game");
        s.getUserProperties().put("gameid", "lobby");

    }

}//eof
