library c4;

import 'dart:html';
import 'dart:async';
import 'dart:convert';

part 'GameBoard.dart';
part 'WSHandler.dart';
part 'Game.dart';
part 'eventhandlers.dart';

const int NO_GAME = 0;
const int GAME_STARTED = 1;
const int GAME_JOINED = 2;
const int PLAYER_CHOSEN = 3;

bool debug = false;
CanvasElement canvas;
String defaultGameId = "lobby";
//String webSocketAddress = "wss://damcode.duckdns.org/c4webserver/${defaultGameId}/c4";
String webSocketAddress = "wss://localhost/c4webserver/${defaultGameId}/c4";
WSHandler ws;
Game game;
SpanElement msgBoxMsg;
TextInputElement chatInput, nickNameInput;
UListElement chatOutput;
ImageElement startGameButton, joinGameButton;
DivElement chatBox, gameBox, playerImageChooser, chatContainer, moveTokenIndicator, playBar, msgBox, scoreBox;
ButtonInputElement msgBoxOk, msgBoxNo;
Map<int, ImageElement> images;
int selectedImageId = 0;
int disableImageId = 0;
int gameState = NO_GAME;
bool chatContainerMoving = false;

void main() {

  nickNameInput = querySelector('#nickname');
  canvas = querySelector('#mainCanvas');
  gameBox = querySelector('#gamebox');
  chatBox = querySelector('#chatbox');
  playerImageChooser = querySelector('#playerimages');
  chatOutput = querySelector('#output');
  chatInput = querySelector('#chatinput');
  startGameButton = querySelector('#imgstartc4');
  joinGameButton = querySelector('#imgjoinc4');
  chatContainer = querySelector('#chatcontainer');
  moveTokenIndicator = querySelector('#movetoken');
  playBar = querySelector('#playbar');
  msgBox = querySelector('#msgbox');
  msgBoxMsg = querySelector('#msgboxmsg');
  msgBoxOk = querySelector('#msgboxok');
  msgBoxNo = querySelector('#msgboxno');
  scoreBox = querySelector('#scorebox');
  
  setupAllEventHandlers();  // event handlers in seperate file for tidyness
    
  ws = new WSHandler(webSocketAddress);
  game = new Game(canvas, ws);
  canvas.style.display = 'none';
  game.start();

}

void setTokenText(String str){
  moveTokenIndicator.innerHtml = str;
}


void updateScoreBox(){
  scoreBox.innerHtml = "Match Score: ${game.wins} wins and ${game.losses} losses!"; 
}

void clearImageSelection(Map<int, ImageElement> images) {
  for (var key in images.keys) {
    ImageElement ie = images[key];
    ie.style.boxShadow = '5px 5px 5px #888888';
    ie.style.border = '1px outset buttonshadow';
    if(key != disableImageId){ 
      ie.style.display = 'inline-block';
    }
  }
}

void createMessageBox(String msg){
  msgBoxMsg.text = msg;
  msgBox.style.display = 'block';
}

void addServerMessage(String msg) {
  var newmsg = new LIElement();
  newmsg.text = msg;
  newmsg.style.color = 'blue';
  chatOutput.children.add(newmsg);
  chatBox.scrollTop = chatBox.scrollHeight;
}

void printdb(var obj) {
  if (debug) {
    print(obj);
  }
}

void showCanvas(bool show) {
  if (show) {
    canvas.style.display = 'block';
  } else {
    canvas.style.display = 'none';
  }
}

void addLocalMessage(String msg) {
  var newmsg = new LIElement();
  newmsg.text = msg;
  chatOutput.children.add(newmsg);
  chatBox.scrollTop = chatBox.scrollHeight;
}

void doChatInput(KeyboardEvent e) {
  if (e.keyCode == KeyCode.ENTER) {
    if (chatInput.value.isEmpty) return;

    ws.sendData(WSHandler.MSG_CHAT, chatInput.value);
    addLocalMessage("Sent: ${chatInput.value}");
    chatInput.value = "";
  }
}


void addSelectImages(var div, Map<int, ImageElement> images){
  for (var e in images.keys) {
    var ie = images[e];
    ie.style.display = 'inline-block';
    ie.className = 'playerselectbutton';
    ie.onClick.listen((r) => setPlayerImage(e));
    div.children.add(ie);
  }
}

void setPlayerImage(int e){
  clearImageSelection(images);
  ImageElement ie = images[e];
  ie.style.boxShadow = '0px 0px 0px #000000';
  ie.style.border = '2px solid green';
  selectedImageId = e;
  game.p1 = images[selectedImageId];
  printdb("selected: ${e}");
  if(ws.ws.readyState == WebSocket.OPEN){
    if(gameState == GAME_JOINED || gameState == GAME_STARTED){
      ws.sendData(WSHandler.MSG_PLAYER_SELECT, selectedImageId.toString());
      gameState = PLAYER_CHOSEN;
      ws.sendReadyMessage();
      setTokenText('Waiting...');
      
    } else if(gameState == PLAYER_CHOSEN){
      ws.sendData(WSHandler.MSG_PLAYER_SELECT, selectedImageId.toString());
      
    }
  }
}


void disableImage(int imageKey){
  printdb("disable image: ${imageKey}");
  disableImageId = imageKey;
  if(imageKey != 0){
    game.p2 = images[disableImageId];  
  }
  for(var e in images.keys){
    ImageElement ie  = images[e];
    if(e == imageKey){
      ie.style.display = 'none';
    } else {
      if(disableImageId == selectedImageId){
       // setPlayerImage(e); 
        printdb('problem!'); // TODO : resolve potential dupe icon due to network latency
      }
      ie.style.display = 'inline-block';
    }
  }
}

void fullResetGame(){
  game.gb.reset();
  gameState = NO_GAME;
  playerImageChooser.style.display = 'none';
  playBar.style.display = 'none';
  selectedImageId = 0;
  disableImage(0);
  clearImageSelection(images);
  showCanvas(false);
  msgBox.style.display = 'none';
  addLocalMessage("Returning to lobby...");
  scoreBox.innerHtml = 'Scores reset!';
}


void setNickname(MouseEvent event) {
  String nickname = nickNameInput.value;
  window.localStorage['nickname'] = nickname;
  ws.sendData(WSHandler.MSG_PLAYER_NAME, nickname);
  addServerMessage("Name set to: ${nickname}");
}
