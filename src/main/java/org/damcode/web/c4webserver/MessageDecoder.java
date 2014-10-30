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

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import static org.damcode.web.c4webserver.Utils.printSysOut;

/**
 *
 * @author dm
 */
public class MessageDecoder implements Decoder.Text<MessageBean> {

    @Override
    public MessageBean decode(String s) {
        MessageBean msg = new MessageBean();
        try {
            JsonObject jObj = Json.createReader(new StringReader(s)).readObject();
            int cmd = jObj.getInt("cmd");
            msg.setCommand(cmd);
            msg.setData(jObj.getString("data"));
            printSysOut("MessageDecoder:" + s + " : " + Integer.toHexString(cmd));
        } catch (Exception e) {
            printSysOut("EXCEPTION!::" + e);
        }
        return msg;
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

}
