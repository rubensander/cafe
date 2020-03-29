var img = new Image();
img.src = "./img/DE_t.svg";


function changeTables() {
    for(let i = 0; i < 5; i++) {
        resetTable(i);
    }
}

function addGuests() {
    for(let i = 0; i < 12; i++) {
        set(drawCard(), i);
    }
}
