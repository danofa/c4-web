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
import org.json.JSONObject;

/**
 *
 * @author dm
 */
public class Utils {

    /**
     * @param name name of sender (server, player).
     * @param data data to be sent.
     * @return string to be sent with Session.getBasicRemote().sendText();
     * @see #assembleChatMessage(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    public static String assembleChatMessage(String name, String data) {
        return assembleChatMessage(name, data, "");
    }

    /**
     * @param name name of sender (server, player).
     * @param data data to be sent.
     * @param source source of message, blank if no source, value if from
     * specific "room"
     * @return string to be sent with Session.getBasicRemote().sendText();
     * @see #assembleChatMessage(java.lang.String, java.lang.String)
     *
     */
    public static String assembleChatMessage(String name, String data, String source) {

        JSONObject msgData = new JSONObject()
                .put("msg", data)
                .put("name", name);

        if (source.length() != 0) {
            source = " (" + source + ") ";
            msgData.put("source", source);
        }

        JSONObject msg = new JSONObject()
                .put("cmd", MessageBean.MSG_CHAT)
                .put("data", msgData);

        return msg.toString();
    }

    public static void printSysOut(String msg) {
        if (Server.DEBUG) {
            System.out.println(msg);
        }
    }

    public static void sendDataMessage(int cmd, String data, Session session) {
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
