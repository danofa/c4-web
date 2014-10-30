part of c4;

class Game {

  CanvasElement canvas;
  CanvasRenderingContext2D context2d;
  ImageElement boardImage;
  ImageElement p1, p2;
  int mouseX = 0;
  int mouseY = 40;
  int dropStep = 40;
  num dropX = 0;
  GameBoard gb;
  int animId;
  bool resetft = false;
  WSHandler ws;
  var wins = 0, losses = 0;

  Game(this.canvas, this.ws) {
    context2d = canvas.context2D;
  }
  
  void start(){
    loadImages();
  }
  
  void mouseClick(MouseEvent event) {
    int mousePos = getMouseColPos();

    if (!gb.pieceDropping2 &&!gb.pieceDropping && ws.hasMoveToken) {
      int pos = gb.addPiece(mousePos,1);

      if (pos != -1) {
        gb.pieceDropping = true;
        ws.sendData(WSHandler.MSG_MOVE_ACTION, "${mousePos}");
        ws.sendData(WSHandler.MSG_PASS_MOVE_TOKEN, "none");
        setTokenText('Waiting...');
        ws.hasMoveToken = false;
      }
    }
  }


  void mouseMove(MouseEvent event) {
    int x = event.offset.x;
    if (x < 10 || x >= boardImage.width * 7) {} else {
      mouseX = event.offset.x;
    }
  }

  void continueLoading() {
    gb = new GameBoard(boardImage, 0, 80);
    ws.board = gb;

    animId = window.requestAnimationFrame(gameLoop);

    canvas.onMouseMove.listen(mouseMove);
    canvas.onClick.listen(mouseClick);
    window.onBlur.listen((e) => (cancelAnim(true)));
    window.onFocus.listen((e) => (cancelAnim(false)));
    
    addSelectImages(playerImageChooser, images);
    playerImageChooser.style.display = 'none';
    playBar.style.display = 'none';
  }

  void cancelAnim(bool b) {
    if (b) {
      if (animId == null) {
        animId = window.requestAnimationFrame(gameLoop);
      }
      window.cancelAnimationFrame(animId);
      animId = null;
      resetft = true;
    } else {
      if (animId == null) {
        animId = window.requestAnimationFrame(gameLoop);
      }
    }
  }

  int getMouseColPos() {
    double mlocX = mouseX / boardImage.width;
    return mlocX.toInt();
  }

  void loadImages() {
    boardImage = new ImageElement(src: "images/boardtile.png");
    images = new Map<int, ImageElement>();

    images[1] = new ImageElement(src: "images/p1.png");
    images[2] = new ImageElement(src: "images/p2.png");
    images[3] = new ImageElement(src: "images/p3.png");
    images[4] = new ImageElement(src: "images/p4.png");
    images[5] = new ImageElement(src: "images/p5.png");
    images[6] = new ImageElement(src: "images/p6.png");
    
    p1 = images[1];
    p2 = images[2];
    
    List imgLoad = new List();
    for ( ImageElement ie in images.values){
      imgLoad.add(ie.onLoad.first); 
    }
    Future.wait(imgLoad).then((_) => continueLoading());
  }

  void drawMouse(ImageElement img, int x, int y) {
    num ix = img.width / 2;
    num iy = img.height / 2;
    context2d.drawImage(img, x - ix, y - iy);
  }


  void render() {
    context2d.clearRect(0, 0, canvas.width, canvas.height);
    double mlocX = mouseX / boardImage.width;
    int lx = getMouseColPos() * boardImage.width;
    drawMouse(p1, lx + 35, mouseY);

    gb.render(context2d, p1, p2);

  }

  int ac = 0;
  void animate() {
    ac++;
    if (ac % 2 == 0) { // 2 second anim
      mouseY = 50;
    } else {
      mouseY = 40;
    }

    if (ac == 9999999) ac = 0;
  }

  int fps = 0;
  num ft = 0;
  int dropEnd = 0;

  void gameLoop(num time) {
    fps++;

    // stop the anim catchup due to window losing focus;
    if (resetft) {
      ft = time - 2000;
      resetft = false;
    }

    if (time - ft > 1000) {
      ft += 1000;
      fps = 0;
      animate(); // execute animations every 1 second;
    }

    gb.update();
    render();
    
    animId = window.requestAnimationFrame(gameLoop);
  }
  
} //eof