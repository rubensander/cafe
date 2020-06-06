//var img = new Image();
//img.src = "./img/DE_t.svg";

const ws = new WebSocket("ws://" + location.host.slice(0,-1) + "5");
var validMoves;
const hand = document.getElementById("hand");
var name = "";
var selectedCard = -1;

// before the start of the game

function joinGame() {
  name = document.getElementById("inpPlayerName").value;
  if(ws.readyState == 0) {
    console.log("Websocket connection has not yet been established.");
  } else if(name == "") {
    console.log("Name must not be empty");
  } else {
    ws.send(JSON.stringify({ status:"JOIN", name }));
  }
}

function submitName(event) {
  if(event.keyCode === 13) {
    joinGame();
  }
}

function addPlayer() {
  ws.send(document.getElementById("inpPlayerName").value);
}

function startGame() {
  ws.send(JSON.stringify({ status:"START" }));
}

// websocket callback functions

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
      for(node of hand.childNodes) {
        hand.removeChild(node);
      }
      let i = 0;
      for(card of msgObj.cards) {
        addHandcard(card);
      }
    case "VALID_MOVES":
      validMoves = msgObj.validMoves;
      break;
    case "TURN_OF":
      if(msgObj.yourName != undefined)
        name = msgObj.yourName;
      if(msgObj.player == name) {
        document.getElementById("whoseTurn").textContent = "– Du bist am Zug";
        for(node of hand.childNodes) {
          node.draggable = true;
        }
      } else {
        document.getElementById("whoseTurn").textContent = "– " + msgObj.player + " ist am Zug";
        document.getElementById("btnEndTurn").style.display = "none";
      }
      break;
    case "DRAWN":
      addHandcard(msgObj.card);
      break;
    case "ERR":
      document.getElementById("websocketStatus").style.display = "none";
      document.getElementById("whoseTurn").style.display = "none";
      document.getElementById("error").style.display = "inline-block";
      document.getElementById("error").textContent = msgObj.message;
      var interval = setInterval(revert, 1000);

      function revert() {
        document.getElementById("websocketStatus").style.display = "inline-block";
        document.getElementById("whoseTurn").style.display = "inline-block";
        document.getElementById("error").style.display = "none";
        clearInterval(interval);
      }
  }
};

ws.onclose = function(code, reason) {
    document.getElementById("websocketStatus").style.color = "#CC0000";
    document.getElementById("websocketStatus").textContent  = "Nicht verbunden";
}

// drag and drop of cards

function drag(ev) {
   ev.dataTransfer.setData("imgSrc", ev.target.src);
   var cardNr;
   for(cardNr = 0; cardNr < hand.childElementCount; cardNr++) {
     if(hand.childNodes[cardNr] == ev.target) break;
   }
   ev.dataTransfer.setData("cardNr", cardNr);
   ws.send(JSON.stringify({ status:"GET_VALID_MOVES", cardNr }));
}

function dragEnter(ev) {
  if(validMoves[ev.target.id.slice(4)] == "NONE") {
    ev.target.style.border = "solid #00CC00";
  }
}

function dragLeave(ev) {
  ev.target.style.border = "";
}

function allowDrop(ev) {
  ev.preventDefault();
}

function drop(ev) {
  ev.preventDefault();
  var seatNr = ev.target.id.slice(4);
  var cardNr = ev.dataTransfer.getData("cardNr");

  if(validMoves[seatNr] == "NONE") {
    ev.target.style.border = "";
    ev.target.src = ev.dataTransfer.getData("imgSrc");
    ws.send(JSON.stringify({ status:"SET_CARD", cardNr, seatNr }));
    hand.removeChild(hand.childNodes[cardNr]);
    document.getElementById("btnEndTurn").style.display = "inline-block";
  } else {
    console.log("This is not a valid move: " + validMoves[seatNr]);
  }
}

function selectCard(ev) {
  if(selectedCard > -1 && selectedCard < hand.childElementCount)
    hand.childNodes[selectedCard].style.border = "";

  var newCard;
  for(newCard = 0; newCard < hand.childElementCount; newCard++) {
    if(hand.childNodes[newCard] == ev.target) break;
  }
  if(newCard == selectedCard) {
    selectedCard = -1;
  } else {
    selectedCard = newCard;
    ev.target.style.border = "solid #00CC00";
    ws.send(JSON.stringify({ status:"GET_VALID_MOVES", cardNr:newCard }));
  }
}

function setIfSelected(ev) {
  ev.preventDefault();
  var seatNr = ev.target.id.slice(4);

  if(validMoves[seatNr] == "NONE") {
    ev.target.style.border = "";
    ev.target.src = hand.childNodes[selectedCard].src;
    ws.send(JSON.stringify({ status:"SET_CARD", cardNr: selectedCard, seatNr }));
    hand.removeChild(hand.childNodes[selectedCard]);
    document.getElementById("btnEndTurn").style.display = "inline-block";
    selectedCard = -1;
  } else {
    console.log("This is not a valid move: " + validMoves[seatNr]);
  }
}

function endTurn() {
   ws.send(JSON.stringify({ status:"END_TURN" }));
}

function drawCard() {
  ws.send(JSON.stringify({ status:"DRAW" }));
}

function addHandcard(card) {
  var img = document.createElement("img");
  img.src = "./img/" + card + ".svg";
  img.ondragstart = drag;
  img.draggable = false;
  img.onclick = selectCard;
  img.className = "handcard";
  //img.style.left = document.getElementById("game").width + i++ / msgObj.cards.length * 100 + "%";
  hand.appendChild(img);
}
