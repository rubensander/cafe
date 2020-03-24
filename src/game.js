var nations = ["DE", "FR", "IT", "TR","DE", "FR", "IT", "TR","DE", "FR", "IT", "TR"]; //["af", "cn", "cu", "de", "en", "fr", "in", "it", "nl", "be"];
var tables = [];
var seats = [];
var stack = [];
var stackCounter;
var tableStack = [];
var tableStackCounter;
var players = [];
var specialMode = "none";
var out = document.getElementsByName("span_out")[0];

init();

function init() {
    for(let i = 0; i < 5; i++) {
        tables[i] = undefined;
    }
    for(let i = 0; i < 12; i++) {
        seats[i] = undefined;
    }
    stackCounter = 2 * 4 * 12;
    for(let i = 0; i < stackCounter; i++) {
        stack[i] = { nation: nations[i % 12], sex: (i % 2 ? "f" : "m")  };
    }
    tableStackCounter = 2 * 12;
    for(let i = 0; i < tableStackCounter; i++) {
        tableStack[i] = { nation: nations[i % 12] };
    }

    stack = shuffle(stack);
    tableStack = shuffle(tableStack);
}

function set(card, table_nr) {
    if(card.nation == tables[table_nr].nation) {
        out.appendChild(document.createTextNode("JA"));
    } else {
        //out.write("NÖ<br>");
    }
}

function resetTable(tableNr) {
    tables[tableNr] = tableStack[tableStackCounter--];
    console.log(tables[tableNr].nation);
    document.getElementById("table" + tableNr).src = "./img/" + tables[tableNr].nation + "_t.svg";
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
