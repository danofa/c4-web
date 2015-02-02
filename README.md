# c4-web

### A connect 4 clone web based multiplayer game, written with java and dart

* 6 selectable icons for play.
* Global chat room for connecting and organising games
* Draggable chat window
* Uses websockets for game data and chat messaging.

![screen shot](https://cloud.githubusercontent.com/assets/6975806/5999951/0e01ba40-aad8-11e4-9bb1-92807698806c.png)
![screen shot](https://cloud.githubusercontent.com/assets/6975806/5999952/0e053e04-aad8-11e4-812a-06255c590de7.png)
![screen shot](https://cloud.githubusercontent.com/assets/6975806/5999953/0e058058-aad8-11e4-93ed-8051b6dd9b38.png)

## What I coded:

I decided to do a web version of the game I had already made to learn a different approach to web programming. The client is written completely in dart compiled to javascript. So the source code is very organised and very maintainable. It connects to a java websocket endpoint and passes json formatted strings containing the data needed by the server. All messages are transmitted like this between client and server.

The client is completely disconnected from the server in terms of logic and game session tracking. It is basically a renderer for the data is recieves from the server, it does do some client side checking for speed sake, but all player moves and actions are tracked and checked by the server, to prevent possible client side 'hacking'.

## Where is it?

Its here: [C4 Battle Arena!](http://damcode.duckdns.org/c4webserver) - Any bugs or issue submissions are welcome.
Enjoy!
