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

/**
 *
 * @author dm
 */
public class MessageBean {
    
    public static final int MSG_CHAT = 0x3e1;
    public static final int MSG_PASS_MOVE_TOKEN = 0x3e2;
    public static final int MSG_MOVE_ACTION = 0x3e3;
    public static final int MSG_START_GAME = 0x3e4;
    public static final int MSG_PLAYER_NAME = 0x3e5; // 997
    public static final int MSG_RESUME_GAME = 0x3e6;
    public static final int MSG_PLAYER_SELECT = 0x3e7;
    public static final int MSG_JOIN_GAME = 0x3e8;
    public static final int MSG_JOIN_LOBBY_CHAT = 0x3e9;
    public static final int MSG_WIN = 0x3ea;
    public static final int MSG_LOSE = 0x3eb;
    public static final int MSG_GAME_AVAILABLE = 0x3ec;
    public static final int MSG_PLAYER_READY = 0x3ed;
    public static final int MSG_GAME_REFRESHBOARD = 0x3ee;
    public static final int MSG_PLAYER_QUIT = 0x3ef;
    public static final int MSG_PLAYER_DISC = 0x3f1;

    
    private int command;
    private String data;

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    
    
}
