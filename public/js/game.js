//var img = new Image();
//img.src = "./img/DE_t.svg";

const ws = new WebSocket("ws://" + location.host.slice(0,-1) + "5");

function joinGame() {
  var name = document.getElementById("inpPlayerName").value;
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
  document.getElementById("websocketStatus").style.color = "#00CC00";
  document.getElementById("websocketStatus").textContent  = "Verbunden";
};

ws.onmessage = function(event) {
  var msgObj = JSON.parse(event.data);
  console.log(msgObj);
  switch(msgObj.status) {
    case "NEW_PLAYER":
      if(msgObj.enableStart) {
        document.getElementById("btnStartGame").style.display = "inline-block";
      }
      document.getElementById("joinGame").style.display = "none";
      document.getElementById("playerList").textContent = msgObj.players[0];
      for(let i = 1; i < msgObj.players.length; i++) {
        document.getElementById("playerList").textContent += ", " + msgObj.players[i];
      }
      break;
    case "STARTED":
      document.getElementById("welcome").style.display = "none";
      document.getElementById("startGame").style.display = "none";
      document.getElementById("game").style.display = "inline-block";
      break;
    case "BOARD":
      for(let iTable = 0; iTable < 5; iTable++) {
         document.getElementById("table" + iTable).src = "./img/" + msgObj.tables[iTable] + ".svg";
      }
      for(let iSeat = 0; iSeat < 12; iSeat++) {
        //if(state.seats[iSeat].card) {
          document.getElementById("seat" + iSeat).src = "./img/" + msgObj.seats[iSeat] + ".svg";
        //}
      }
    case "HAND":
      var hand = document.getElementById("hand");
      for(card of msgObj.cards) {
        var imgElement = document.createElement("img");
        imgElement.setAttribute("src", "./img/" + card + ".svg");
        imgElement.setAttribute("alt", "na");
        hand.appendChild(imgElement);
      }
  }
};

ws.onclose = function(code, reason) {
    document.getElementById("websocketStatus").style.color = "#CC0000";
    document.getElementById("websocketStatus").textContent  = "Nicht verbunden";
}

function startGame() {
  ws.send(JSON.stringify({ status:"START" }));
}

/*
ws.onopen = function() {
  ws.send('Hallo an Server!');
};

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
}*/
