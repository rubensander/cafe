const ws = new WebSocket("ws://" + location.host.slice(0,-1) + "4");

ws.onopen = function() {
  ws.send(JSON.stringify({ state:"PLAYERS" }));
};

ws.onmessage = function(event) {
  console.log(event.data);
  document.getElementById("playerList").textContent = event.data;
};

function startGame() {
  ws.send(JSON.stringify({ state:"START" }));
}
