const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const game = require('./game');
const spawn = require('child_process').spawn;
const javagame = spawn('java', )

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use(express.static(__dirname + "/public"));
//app.use(function (req, res, next) {
//  res.sendFile(__dirname + "/public/start.html");
//});
app.get("", (req, res) => res.sendFile(__dirname + "/public/start.html"));
app.get("/game", function(req, res) {
  res.sendFile(__dirname + "/public/game.html");
});
app.post("/join", (req, res) => {
  if(req.body.playerName != undefined) {
    console.log(req.body.playerName);
    game.addPlayer(req.body.playerName, req.ip);
    res.sendFile(__dirname + "/public/start_ready.html");
  }
});
app.listen(2853, () => console.log("Server running on port 2853"));

var playerID = 0;

/*const httpServer = http.createServer(function (req, res) {
  console.log(req);
  //res.writeHead(200, {'Content-Type': 'text/plain'});
  res.write('Hello World!');
  res.end();
});*/

const wss = new WebSocket.Server({
  port:2854
});

wss.on("connection", function(wss, req) {
  wss.on("message", (message) => {
    parseMessage(req, message)
  });
  wss.on("close", () => {});
});

function parseMessage(req, messageString) {
  var message = JSON.parse(messageString);
  console.log(message);
  switch(message.state) {
    case "PLAYERS":
      // if a new player says hello, broadcast playerlist
      let playerList = game.playerList();
      wss.clients.forEach(function each(client) {
        if (client.readyState === WebSocket.OPEN) {
          client.send(playerList);
        }
      });
      break;
    case "CHANGE":
      for(let i = 0; i < 5; i++) {
        game.resetTable(i);
      }
      wss.send(JSON.stringify(game.state));
      break;
    default:
      console.log("Unknown message: " + messageStr);
  }
}
