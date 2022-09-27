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
    ws.send(JSON.stringify({ msgType:"JOIN", name }));
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
  ws.send(JSON.stringify({ msgType:"START" }));
}

// websocket callback functions

ws.onopen = function() {
  document.getElementById("websocketStatus").style.color = "#00CC00";
  document.getElementById("websocketStatus").textContent  = "Verbunden";
};

ws.onmessage = function(event) {
  var msgObj = JSON.parse(event.data);
  console.log(msgObj);
  switch(msgObj.msgType) {
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
        seat = document.getElementById("seat" + iSeat);
        seat.src = "./img/" + msgObj.seats[iSeat] + ".svg";
        if(msgObj.seats[iSeat] == "XX_x") {
          seat.style.borderColor = "";
          seat.style.borderStyle = "";
        }
      }
      if(msgObj.specialMode == "SECONDCARD" || msgObj.specialMode == "CIRCLE") {
        document.getElementById("btnEndTurn").style.display = "none";
        document.getElementById("btnTakeBackCard").style.display = "inline-block";
      } else {
        document.getElementById("btnTakeBackCard").style.display = "none";
      }
      break;
    case "HAND":
    // TODO
      for(let i = hand.childElementCount - 1; i >= 0; i--) {
        hand.removeChild(hand.childNodes[i]);
      }
      for(card of msgObj.cards) {
        addHandcard(card);
      }
      break;
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
      setTimeout(function () { document.getElementById("hand").scrollLeft = 5000; }, 100);
      break;
    case "INFO":
      document.getElementById("points").textContent = msgObj.points;
      document.getElementById("btnEndTurn").style.display = msgObj.canEndTurn ? "inline-block" : "none";
      document.getElementById("btnTakeBackCard").style.display = msgObj.canTakeBackCard ? "inline-block" : "none";
      break;
    case "END":
      if(document.getElementById("whoseTurn").textContent === "– Du bist am Zug") {
        wsEnd = new WebSocket("ws://" + location.host.slice(0,-1) + "5");
        setTimeout(function() {
          ws.close();
        }, 1000);
      }
      document.getElementById("whoseTurn").textContent = msgObj.winners + " hat gewonnen!";
      ws.send(JSON.stringify({ msgType:"GAME_ENDED" }));
      document.getElementById("websocketStatus").style.display = "none";
      break;
    case "POINTS":
      document.getElementById("points").textContent = msgObj.points;
      break;
    case "END":
      if(document.getElementById("whoseTurn").textContent === "– Du bist am Zug") {
        wsEnd = new WebSocket("ws://" + location.host.slice(0,-1) + "5");
        setTimeout(function() {
          ws.close();
        }, 1000);
      }
      document.getElementById("whoseTurn").textContent = msgObj.winners + " hat gewonnen!";
      ws.send(JSON.stringify({ status:"GAME_ENDED" }));
      document.getElementById("websocketStatus").style.display = "none";
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
};

ws.onerror = function(event) {
  if(event.target.readyState == 3) {
    document.getElementById("websocketStatus").style.color = "#CC0000";
    document.getElementById("websocketStatus").textContent  = "Nicht verbunden";
  }
}

// drag and drop of cards

function drag(ev) {
   ev.dataTransfer.setData("imgSrc", ev.target.src);
   var cardNr;
   for(cardNr = 0; cardNr < hand.childElementCount; cardNr++) {
     if(hand.childNodes[cardNr] == ev.target) break;
   }
   ev.dataTransfer.setData("cardNr", cardNr);
   ws.send(JSON.stringify({ msgType:"GET_VALID_MOVES", cardNr }));
}

function dragEnter(ev) {
  validity = validMoves[ev.target.id.slice(4)];
  if(validity == "NONE") {
    ev.target.style.borderColor = "#00CC00";
    ev.target.style.borderStyle = "solid";
  } if(validity == "ONLY_IN_CIRCLE") {
    ev.target.style.borderColor = "#CCCC00";
    ev.target.style.borderStyle = "solid";
  }
}

function dragLeave(ev) {
  ev.target.style.borderColor = "";
  ev.target.style.borderStyle = "";
}

function allowDrop(ev) {
  ev.preventDefault();
}

function drop(ev) {
  selectedCard = ev.dataTransfer.getData("cardNr");
  setIfSelected(ev);
}

function selectCard(ev) {
  if(selectedCard > -1 && selectedCard < hand.childElementCount) {
    hand.childNodes[selectedCard].style.borderColor = "";
    hand.childNodes[selectedCard].style.borderStyle = "";
  }

  var newCard;
  for(newCard = 0; newCard < hand.childElementCount; newCard++) {
    if(hand.childNodes[newCard] == ev.target) break;
  }
  if(newCard == selectedCard) {
    selectedCard = -1;
  } else {
    selectedCard = newCard;
    ev.target.style.borderColor = "#00CC00";
    ev.target.style.borderStyle = "solid";
    ws.send(JSON.stringify({ msgType:"GET_VALID_MOVES", cardNr:newCard }));
  }
}

function setIfSelected(ev) {
  ev.preventDefault();
  var seatNr = ev.target.id.slice(4);

  if(validMoves[seatNr] == "NONE") {
    ev.target.style.borderColor = "";
    ev.target.style.borderStyle = "";
  } else if(validMoves[seatNr] == "ONLY_IN_CIRCLE") {
    ev.target.style.borderColor = "#CCCC00";
    ev.target.style.borderStyle = "solid";
  } else {
    console.log("This is not a valid move: " + validMoves[seatNr]);
    return;
  }
  ev.target.src = hand.childNodes[selectedCard].src;
  ws.send(JSON.stringify({ msgType:"SET_CARD", cardNr: selectedCard, seatNr }));
  hand.removeChild(hand.childNodes[selectedCard]);
  selectedCard = -1;
}

function endTurn() {
   ws.send(JSON.stringify({ msgType:"END_TURN" }));
}

function drawCard() {
  ws.send(JSON.stringify({ msgType:"DRAW" }));
}

function takeBackCard() {
  ws.send(JSON.stringify({ msgType:"TAKE_BACK_CARD" }));
}

function takeBackCard() {
  ws.send(JSON.stringify({ status:"TAKE_BACK_CARD" }));
}

function addHandcard(card) {
  var img = document.createElement("img");
  img.src = "./img/" + card + ".svg";
  img.ondragstart = drag;
  img.draggable = (document.getElementById("whoseTurn").textContent === "– Du bist am Zug");
  img.onclick = selectCard;
  img.className = "handcard";
  //img.style.left = document.getElementById("game").width + i++ / msgObj.cards.length * 100 + "%";
  hand.appendChild(img);
}

// First we get the viewport height and we multiple it by 1% to get a value for a vh unit
let vh = window.innerHeight * 0.01;
// Then we set the value in the --vh custom property to the root of the document
document.documentElement.style.setProperty('--vh', `${vh}px`);
