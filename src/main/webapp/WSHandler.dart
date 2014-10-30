part of c4;

class WSHandler {
  WebSocket ws;
  bool hasMoveToken = false;
  GameBoard board;
  String url;

  static const int MSG_CHAT = 0x3e1;
  static const int MSG_PASS_MOVE_TOKEN = 0x3e2;
  static const int MSG_MOVE_ACTION = 0x3e3;
  static const int MSG_START_GAME = 0x3e4;
  static const int MSG_PLAYER_NAME = 0x3e5; // 997
  static const int MSG_RESUME_GAME = 0x3e6;
  static const int MSG_PLAYER_SELECT = 0x3e7;
  static const int MSG_JOIN_GAME = 0x3e8;
  static const int MSG_JOIN_LOBBY_CHAT = 0x3e9;
  static const int MSG_WIN = 0x3ea;
  static const int MSG_LOSE = 0x3eb;
  static const int MSG_GAME_AVAILABLE = 0x3ec;
  static const int MSG_PLAYER_READY = 0x3ed;
  static const int MSG_GAME_REFRESHBOARD = 0x3ee;
  static const int MSG_PLAYER_QUIT = 0x3ef;
  static const int MSG_PLAYER_DISC = 0x3f1;

  WSHandler(this.url) {
    wsConnect(url);
  }

  void wsConnect(url) {
    printdb(url);
/*   
 * TODO: broken link reconnect code.
 *  if (window.sessionStorage.containsKey("gameid")) {
      var gameid = window.sessionStorage['gameid'];
      url = "ws://localhost/c4webserver/${gameid}/c4";
      print(url);
    }
  */
//    var client = new HttpClient();
//    var u = new Uri.fromString(url);
//    var conn = new WebSocketClientConnection(client.getUrl(u));
//    conn.onOpen.listen(onOpen);
//    conn.onMessage.listen(onMessage);
//    conn.onClose.listen(onClose);
    
    ws = new WebSocket(url);
    ws.onOpen.listen(onOpen);
    ws.onMessage.listen(onMessage);
    ws.onClose.listen(onClose);
  }

  void onClose(Event e) {
    addServerMessage("Connection Closed!");

    var msglink = new AnchorElement();
    msglink.href = "#";
    msglink.onClick.listen((e) {
      wsConnect(url);
    });
    msglink.text = "[ Reconnect? ]";
    chatOutput.children.add(msglink);
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  void onOpen(Event e) {
    addServerMessage("CONNECTED");
    setNickname(null);
  }

  void onMessage(MessageEvent me) {
    int cmd;
    var data;
    try {
      printdb(me.data);

      var msg = JSON.decode(me.data);
      cmd = msg['cmd'];
      data = msg['data'];

    } catch (e) {
      addLocalMessage("error in decode json: ${e}");
      return;
    }

    switch (cmd) {
      case WSHandler.MSG_CHAT:
        var name = data['name'];
        var msg = data['msg'];
        var source = data['source'];
        if (source == null) {
          source = " ";
        }
        addServerMessage("${name}${source}> ${msg}");
        break;

      case WSHandler.MSG_START_GAME:
        // addServerMessage("Started game with id: ${data}");
        window.sessionStorage['gameid'] = data;
        break;

      case WSHandler.MSG_PASS_MOVE_TOKEN:
        hasMoveToken = true;
        setTokenText('Your Move');
        break;

      case WSHandler.MSG_MOVE_ACTION:
        board.addOpponentPiece(int.parse(data), 2);
        break;

      case WSHandler.MSG_WIN:
        hasMoveToken = false;
        game.wins = data;
        createMessageBox('You won!');
        updateScoreBox();
        break;

      case WSHandler.MSG_LOSE:
        hasMoveToken = false;
        game.losses = data;
        createMessageBox('You lost!');
        updateScoreBox();
        break;

      case WSHandler.MSG_GAME_AVAILABLE:
        addLocalMessage("- Available games:");
        var msglink = new AnchorElement();
        var li = new LIElement();
        li.children.add(msglink);
        msglink.href = "#";
        msglink.onClick.listen((e) {
          sendData(WSHandler.MSG_JOIN_GAME, data['id']);
          window.sessionStorage['gameid'] = data['id'];
          chatOutput.children.remove(li);
          gameState = GAME_JOINED;
          playBar.style.display = 'block';
          playerImageChooser.style.display = 'block';
          setTokenText('Select ->');
        });
        
        msglink.text = "[ Click here to join: ${data['host']} ]";
        chatOutput.children.add(li);
        chatBox.scrollTop = chatBox.scrollHeight;
        break;

      case WSHandler.MSG_GAME_REFRESHBOARD:
        try {
          List gb = JSON.decode(data);
          game.gb.board = gb;
        } catch (e) {
          addLocalMessage("Broken refresh message.");
        }
        break;

      case WSHandler.MSG_PLAYER_READY:
        game.gb.reset();
        addServerMessage("Game has started!");
        setTokenText('Waiting...');
        showCanvas(true);
        break;

      case WSHandler.MSG_PLAYER_QUIT:
        sendData(WSHandler.MSG_PLAYER_QUIT, "none");
        addServerMessage("Player has quit!");
        fullResetGame();
        break;

      case WSHandler.MSG_PLAYER_DISC:
        addServerMessage("Player has disconnected!");
        fullResetGame();
        break;
        
      case WSHandler.MSG_PLAYER_SELECT:
//        clearImageSelection(images.values);
        disableImage(int.parse(data));
        break;

    }



  }

  void sendRaw(String data) => ws.sendString(data);

  void sendData(int cmd, String data) {
    var sendMsg = {
      'cmd': cmd,
      'data': data
    };
    ws.sendString(JSON.encode(sendMsg));
  }

  void sendReadyMessage(){
    sendData(WSHandler.MSG_PLAYER_READY, "ready");
  }

  
  
}//eof
