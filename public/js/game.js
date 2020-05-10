//var img = new Image();
//img.src = "./img/DE_t.svg";

const ws = new WebSocket("ws://" + location.host.slice(0,-1) + "5");
var validMoves;
//const hand = document.getElementById("hand");

function submitName(event) {
  if(event.keyCode === 13) {
    joinGame();
  }
}

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
      document.getElementById("joinGame").style.display = "none";
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
      break;
    case "HAND":
      for(node of document.getElementById("hand").childNodes) {
        document.getElementById("hand").removeChild(node);
      }
      for(card of msgObj.cards) {
        var img = document.createElement("img");
        img.src = "./img/" + card + ".svg";
        img.style.display = "inline-block";
        img.ondragstart = drag;
        img.draggable = false;
        document.getElementById("hand").appendChild(img);
      }
    case "VALID_MOVES":
      validMoves = msgObj.validMoves;
      break;
    case "YOUR_TURN":
      document.getElementById("whoseTurn").textContent = " – Du bist am Zug";
      for(node of document.getElementById("hand").childNodes) {
        node.draggable = true;
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


// drag and drop of cards
function drag(ev) {
   ev.dataTransfer.setData("imgSrc", ev.target.src);
   var cardNr;
   for(cardNr = 0; cardNr < document.getElementById("hand").childElementCount; cardNr++) {
     if(document.getElementById("hand").childNodes[cardNr] == ev.target) break;
   }
   ev.dataTransfer.setData("cardNr", cardNr);
   ws.send(JSON.stringify({ status:"GET_VALID_MOVES", cardNr }));
}
function drop(ev) {
  ev.preventDefault();
  var seatNr = ev.target.id.slice(4);
  var cardNr = ev.dataTransfer.getData("cardNr");

  if(validMoves[seatNr] == "NONE") {
    ev.target.style.border = "";
    ev.target.src = ev.dataTransfer.getData("imgSrc");
    ws.send(JSON.stringify({ status:"SET_CARD", cardNr, seatNr }));
    document.getElementById("hand").removeChild(document.getElementById("hand").childNodes[cardNr]);
  } else {
    console.log("This is not a valid move: " + validMoves[seatNr]);
  }
}
function allowDrop(ev) {
  ev.preventDefault();
}
function dragEnter(ev) {
  if(validMoves[ev.target.id.slice(4)] == "NONE") {
    ev.target.style.border = "solid #00CC00";
  }
}
function dragLeave(ev) {
  ev.target.style.border = "";
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
