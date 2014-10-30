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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;
import static org.damcode.web.c4webserver.Utils.printSysOut;
import org.json.JSONObject;

/**
 *
 * @author dm
 */
public class Player {

    int boardId;
    int imageId;
    Session session;
    boolean hasTurn = false;
    boolean ready = false;
    int wins = 0;
    int losses = 0;

    public Player(int boardId, Session session) {
        printSysOut("player init id: " + session.getId() + ", board id: " + boardId);
        this.boardId = boardId;
        this.session = session;
    }

    public String getName(){
        return (String) session.getUserProperties().get("name");
    }
    
    public void sendDataMessage(int cmd, String data){
        try {
            String msg = new JSONObject()
                    .put("cmd", cmd)
                    .put("data", data)
                    .toString();

            session.getBasicRemote().sendText(msg);
            
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
