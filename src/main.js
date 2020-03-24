var img = new Image();
img.src = "./img/DE_t.svg";


function showDE() {
    //for (let table of document.getElementsByClassName("table")) {
    //    seat.children[0].src = "./img/DE_t.svg"
    //}

    for(let i = 0; i < 5; i++) {
        resetTable(i);
    }
}
