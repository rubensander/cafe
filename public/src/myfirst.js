const express = require('express');
var http = require('http');
var WebSocket = require('ws');

const app = express();
const port = 3000;
app.get('/', (req, res) => res.send("Hello World!"));

app.listen(port, () => console.log("Example app listening on port ${port}"));

/*
http.createServer(function (req, res) {
  res.writeHead(200, {'Content-Type': 'text/html'});
  res.end('Hello World!');
}).listen(8080);
*/
