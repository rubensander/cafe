const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const game = require('./game');

const app = express();
app.use(express.static(__dirname + "/public"));
//app.use(function (req, res, next) {
//  res.sendFile(__dirname + "/public/start.html");
//});
app.get("", (req, res) => res.sendFile(__dirname + "/public/start.html"));
app.get("/game", function(req, res) {
  res.sendFile(__dirname + "/public/game.html");
});
app.listen(80, () => console.log("Server running on port 80"));

const httpServer = http.createServer(function (req, res) {
  console.log(req);
  //res.writeHead(200, {'Content-Type': 'text/plain'});
  res.write('Hello World!');
  res.end();
});
const wss = new WebSocket.Server({
  server:httpServer
});

var playerID = 0;

wss.on("connection", function(wss, req) {
  wss.send(playerID++);

  wss.on("message", (message) => {
    parseMessage(wss, req, message)
  });
  wss.on("close", () => {});
});

function parseMessage(wss, req, message) {
  if(message == "CHANGE") {
    for(let i = 0; i < 5; i++) {
      game.resetTable(i);
    }
    wss.send(JSON.stringify(game.state));
  }
}


httpServer.listen(8080);
