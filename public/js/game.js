var img = new Image();
var id = -1;
img.src = "./img/DE_t.svg";

function changeTables() {
  ws.send("CHANGE");
}

function addPlayer() {
  ws.send(document.getElementById("inpPlayerName").value);
    /*for(let i = 0; i < 12; i++) {
        set(drawCard(), i);
    }*/
}

const ws = new WebSocket('ws://192.168.2.114:8080');
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
