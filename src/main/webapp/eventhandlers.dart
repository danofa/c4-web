part of c4;

void setupAllEventHandlers(){
  
  nickNameInput.onKeyPress.listen((e) {
    if (e.keyCode == KeyCode.ENTER) {
      querySelector('#setnickname').click();
    }
  });

  joinGameButton.onClick.listen((e) {
      ws.sendData(WSHandler.MSG_GAME_AVAILABLE, "none");
  });

  
  msgBoxNo.onClick.listen((e){
    msgBox.style.display = 'none';
    ws.sendData(WSHandler.MSG_PLAYER_QUIT, "quit!");
    fullResetGame();
  });

  msgBoxOk.onClick.listen((e){
    msgBox.style.display = 'none';
    ws.sendReadyMessage();
    addLocalMessage("Game restarted, waiting on player");
  });
      
  startGameButton.onClick.listen((e) {
    game.gb.reset();
    ws.sendData(WSHandler.MSG_START_GAME, "none");
    gameState = GAME_STARTED;
    playBar.style.display = 'block';
    playerImageChooser.style.display = 'block';
    setTokenText('Select ->');
  });

  
  querySelector('#quitgame').onClick.listen((e){
    ws.sendData(WSHandler.MSG_PLAYER_QUIT, "quit!");
    fullResetGame();
  });
  

  chatBox.onClick.listen((e) => chatInput.focus());


  chatContainer.onMouseDown.listen((e) {
    chatContainerMoving = true;
    chatContainer.style.cursor = 'move';
  });

  chatContainer.onMouseUp.listen((e) {
    chatContainerMoving = false;
    chatContainer.style.cursor = 'auto';
  });

  chatContainer.onMouseMove.listen((e) {
    if (chatContainerMoving) {
      chatContainer.style.userSelect = 'none';
      chatContainer.style.order = "99";
      chatContainer.style.position = 'absolute';
      chatContainer.style.top = "${(e.page.y - 50).toString()}px";
      chatContainer.style.left = "${(e.page.x - 50).toString()}px";
      print(e.page.x.toString());
      print(e.page.y.toString());
    }
  });

  chatInput.onKeyDown.listen(doChatInput);
  
  querySelector('#setnickname').onClick.listen(setNickname);

  if (window.localStorage.containsKey("nickname")) {
    var name = window.localStorage['nickname'];
    nickNameInput.value = name;
  }

}

