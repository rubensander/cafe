const express = require('express');
const http = require('http');
const WebSocket = require('ws');

// Spawn java process
const spawn = require('child_process').spawn;
const javagame = spawn('java', ['-jar', './java-project/cafe.jar']);
javagame.stdout.on('data', (data) => {
  process.stdout.write(`java: ${data}`);
});
javagame.on('close', (code) => {
  console.log(`Java exited with code ${code}`);
});

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
app.listen(2853, () => console.log("Server running on port 2853"));
