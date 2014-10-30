part of c4;

class GameBoard {
  CanvasElement canvas;
  ImageElement boardTile;
  int x, y, w, h, tw, th;
  List<int> board;
  bool canDrop;
  bool pieceDropping = false, pieceDropping2 = false;
  int dropStep = 40;
  int dropEnd, lastDrop;
  num dropX;
  
  GameBoard(ImageElement boardTile, int x, int y) {
    this.boardTile = boardTile;
    this.x = x;
    this.y = y;
    tw = boardTile.width;
    th = boardTile.height;
    w = tw * 7;
    h = th * 6;

    board = new List<int>(6 * 7);
  }

  void reset(){
    board = new List<int>(6 * 7);
  }
  
  void addOpponentPiece(int boardPos, int player){
    board[boardPos] = player;
    lastDrop = boardPos;
    dropX = (boardPos ~/ 6) * 70 + 16 + x;
    pieceDropping2 = true;
  }
  
  int addPiece(int col, int player) {
    for (int i = 0; i < 6; i++) {
      int pos = ((col * 6) + 5) - i;

      if (board[pos] == null || board[pos] == 0) {
        board[pos] = player;
        dropX = col * 70 + 16;
        lastDrop = pos;
        return pos;
      }
    }
    return -1;
  }

  void update(){
    if (pieceDropping || pieceDropping2) {
      dropEnd = h  - ((5 - (lastDrop % 6)) * 70);
      if (dropStep <= dropEnd) {
        dropStep += 8;
      } else {
        pieceDropping = false;
        pieceDropping2 = false;
        dropStep = 40;
      }
    }
  }
  
  
  void render(CanvasRenderingContext2D c2d, ImageElement p1, ImageElement p2) {

    int pieceIndex = 0;

    if (pieceDropping) {
      c2d.drawImage(p1, dropX, dropStep);
    } else if(pieceDropping2){
      c2d.drawImage(p2, dropX, dropStep);
    }

    for (int c = 0; c < 7; c++) {
      for (int r = 0; r < 6; r++) {
        // render board
        c2d.drawImage(boardTile, x + (c * tw), y + (r * th));

        // render player pieces here!

        int boardLoc = board[pieceIndex];
        
        if ((boardLoc != null || boardLoc != 0) && (pieceIndex != lastDrop || (!pieceDropping && !pieceDropping2))) {
          if (boardLoc == 1) {
            c2d.drawImage(
                p1,
                x + (c * tw) + (p1.width / 2),
                y + (r * th) + (p1.height / 2));
          } else if(boardLoc == 2) {
            c2d.drawImage(
                p2,
                x + (c * tw) + (p2.width / 2),
                y + (r * th) + (p2.height / 2));
          }
        }

        pieceIndex++;

      }
    }
  }


}
