//var img = new Image();
//img.src = "./img/DE_t.svg";

const ws = new WebSocket("ws://" + location.host.slice(0,-1) + "5");

function joinGame() {
  var name = document.getElementById("inpPlayerName").value;
  console.log(ws);
  if(ws.readyState == 0) {
    console.log("Websocket connection has not yet been established.");
  } else if(name == "") {
    console.log("Name must not be empty");
  } else {
    ws.send(JSON.stringify({ status:"JOIN", name }));
  }
}

function startGame() {
  ws.send(JSON.stringify({ status:"START" }));
}

function addPlayer() {
  ws.send(document.getElementById("inpPlayerName").value);
    /*for(let i = 0; i < 12; i++) {
        set(drawCard(), i);
    }*/
}


ws.onopen = function() {

};

ws.onmessage = function(event) {
  if(event.data.status == "NEW_PLAYER") {
    if(event.data.enableStart)
      document.getElementById("btnStartGame");
    for(p of event.data.players) {
      document.getElementById("playerList").textContent = p;
    }
  }
  console.log(event.data);
};

function startGame() {
  ws.send(JSON.stringify({ state:"START" }));
}

/*
ws.onopen = function() {
  ws.send('Hallo an Server!');
};*/

ws.onmessage = function(event) {
  if(id == -1) {
    id = event.data;
    document.getElementById("playerinfo").innerHTML = "Player ID: " + id;
  } else {
    showGameState(JSON.parse(event.data));
  }
};

function showGameState(state) {
  for(let iTable = 0; iTable < 5; iTable++) {
     document.getElementById("table" + iTable).src = "./img/" + state.tables[iTable].nation + "_t.svg";
  }
  for(let iSeat = 0; iSeat < 12; iSeat++) {
    if(state.seats[iSeat].card) {
      document.getElementById("seat" + iSeat).src = "./img/" + state.seats[iSeat].nation + "_" + state.seats[iSeat].sex + ".svg";
    }
  }
}
