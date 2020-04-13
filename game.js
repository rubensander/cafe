const nations = ["DE", "FR", "IT", "TR","DE", "FR", "IT", "TR","DE", "FR", "IT", "TR"]; //["af", "cn", "cu", "de", "en", "fr", "in", "it", "nl", "be"];
var tables = [];
var seats = [];
var stack = [];
var stackSize;
var nationStack = [];
var nationStackSize;
var curPlayer;
var specialMode = "none";

function init() {
    for(let i = 0; i < 5; i++) {
        tables[i] = {};
    }
    for(let i = 0; i < 3; i++) {
        seats[3*i] = { center: true, tables: [ tables[i], tables[(i+3)%4], tables[4] ] };
        seats[3*i+1] = { center: false, table: tables[i] };
        seats[3*i+2] = { center: false, table: tables[i] };
    }
    stackSize = 2 * 4 * 12;
    for(let i = 0; i < stackSize; i++) {
        stack[i] = { nation: nations[i % 12], sex: (i % 2 ? "f" : "m")  };
    }
    nationStackSize = 2 * 12;
    for(let i = 0; i < nationStackSize; i++) {
        nationStack[i] = nations[i % 12];
    }

    stack = shuffle(stack);
    nationStack = shuffle(nationStack);
}

function set(card, seatNr) {
    var seat = seats[seatNr];
    if(card == undefined || seatNr < 0 || seatNr > 11) // param error
        return false;
    if(seat.card != undefined) // seat taken error
        return false;

    var tableFits = false;
    if(seat.center) {
        for(table of seat.tables) {
            tableFits |= ( table.nation == card.nation );
        }
    } else {
        tableFits = ( seat.table.nation == card.nation );
    }
    if(!tableFits) // table mismatch error
        return false;

    seat.card = card;
}

function resetTable(tableNr) {
    if(nationStackSize > 0) {
        tables[tableNr].nation = nationStack[--nationStackSize];
    } else {
        endGame();
    }
}

function drawCard() {
    if(stackSize > 0) {
        return stack[--stackSize];
    }
    return;
}

function addPlayer(name, ip) {
    var newPlayer = { points:0, cards:[], name, ip };
    for(let i = 0; i < 5; i++) {
      newPlayer.cards[i] = drawCard();
    }
    if(curPlayer == undefined) {
        newPlayer.next = newPlayer;
    } else {
        newPlayer.next = curPlayer.next;
        curPlayer.next = newPlayer;
    }
    curPlayer = newPlayer;
}

function playerList() {
  // create player list
  var playerList = "";
  var player = curPlayer.next;
  while(player != curPlayer) {
    playerList += player.name + ", ";
    player = player.next;
  }
  return playerList + curPlayer.name;
}

function endGame() {

}

/**
 * Shuffles array in place.
 * @param {Array} a items An array containing the items.
 */
function shuffle(a) {
    var j, x, i;
    for (i = a.length - 1; i > 0; i--) {
        j = Math.floor(Math.random() * (i + 1));
        x = a[i];
        a[i] = a[j];
        a[j] = x;
    }
    return a;
}

init();

exports.playerList = playerList;
exports.state = { tables, seats };
exports.resetTable = resetTable;
exports.addPlayer = addPlayer;
