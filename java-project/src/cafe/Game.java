package cafe;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
//import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Game {
  Table[] tables = new Table[5];
  Seat[] seats = new Seat[12];
  Stack<Card> stack;
  Stack<Nation> tableStack;
  Player curPlayer;
  SpecialMode specialMode;
  public static int cMaxCards = 12;

  ServerSocket webSocket;


  public static void main(String args[]) {

    Game game = new Game();
    try {
      game.startWebSocket();

      while(game.specialMode == SpecialMode.NOTSTARTED) {
    	try {
    		new Thread(new Player(game, game.webSocket.accept()));
    	} catch(Exception ex) {
    		System.out.println("Socket could not connect to player.");
    	}
      }

      game.webSocket.close();
      
      //game.start();
    } catch(IOException ex) {
        System.out.println("Websocket server failed starting: " + ex.getMessage());
      }
  }

  public Game() {
    for(int i = 0; i < 5; i++) {
      tables[i] = new Table(Nation.GERMANY);
    }
    for(int i = 0; i < 4; i++) {
      seats[3*i] = new CenterSeat(tables[i], tables[(i+3)%4], tables[4]);
      seats[3*i+1] = new Seat(tables[i]);
      seats[3*i+2] = new Seat(tables[i]);
    }

    stack = new Stack<Card>();
    for(Nation nation : Nation.values()) {
      for(int j = 0; j < 4; j++) {
        stack.push(new Card(nation, Sex.FEMALE));
        stack.push(new Card(nation, Sex.MALE));
      }
    }
    Collections.shuffle(stack);

    tableStack = new Stack<Nation>();
    for(Nation nation : Nation.values()) {
      tableStack.push(nation);
      tableStack.push(nation);
    }
    Collections.shuffle(tableStack);

    curPlayer = null;
    specialMode = SpecialMode.NOTSTARTED;
    //createSzenario();
  }

  public void startWebSocket() throws IOException {
    webSocket = new ServerSocket(2855);
    System.out.println("Websocket server started on port 2855.");
  }

  public void broadcast(String status, JSONObject data) throws JSONException {
    data.put("status", status);
    String message = data.toString();

    Player p = curPlayer;
    do {
      p.send(message);
      p = p.getNext();
    } while(p != curPlayer);
  }

  public ArrayList<Card> join(Player pSender) {
    // broadcast message of new player having joined
    JSONObject msgObj = new JSONObject();

    try {
      if(curPlayer == null) {
        msgObj.put("enableStart", new Boolean(false));
      } else {
        pSender.setNext(curPlayer.getNext());
        curPlayer.setNext(pSender);
        msgObj.put("enableStart", new Boolean(true));
      }
      curPlayer = pSender;

      // get player list
      JSONArray players = new JSONArray();
      Player p = curPlayer;
      do {
        players.put(p.getName());
        p = p.getNext();
      } while(p != curPlayer);
      msgObj.put("players", players);

      broadcast("NEW_PLAYER", msgObj);
    } catch(JSONException e) {
      System.out.println(e.getMessage());
    } finally {
      curPlayer = pSender;
    }

    // draw start cards
    ArrayList<Card> startCards = new ArrayList<Card>();
    for(int i = 0; i < 7; i++) {
      startCards.add(stack.peek());
      stack.pop();
    }
    return startCards;
  }


  public void start() {/*
    for(int i = 0; i < 5; i++) {
      tables[i].setNation(tableStack.pop());
    }
    if(curPlayer != null) {
      curPlayer = curPlayer.getNext();
      curPlayer.beginTurn();
    }
    specialMode = SpecialMode.FIRSTCARD;
    Printer.printGraphic(seats, tables);
    System.out.println();
    Printer.printPlayerWithIndex(curPlayer);

    Scanner scanner = new Scanner(System.in);
    int iCard, iSeat;
    ErrType err = ErrType.NONE;
    //scanner.useDelimiter("\s");

    loop: while(true) {
      System.out.print("set|");
      if(curPlayer.getCards().size() < cMaxCards) System.out.print("take|");
      else System.out.print("discard|");
      if(specialMode == SpecialMode.SECONDCARD) System.out.print("undo|");
      if(specialMode == SpecialMode.NONE) System.out.print("end|");
      System.out.print("q> ");
      switch(scanner.next()) {
        case "hand":
        Printer.printPlayerWithIndex(curPlayer);
        break;
        case "table":
        Printer.printGraphic(seats, tables);
        break;
        case "set":
        System.out.print("1 to " + curPlayer.getCards().size() + ": ");
        iCard = scanner.nextInt();
        System.out.print("to ");
        iSeat = scanner.nextInt();
        curPlayer.placeCardAt(iCard - 1, seats[iSeat]);
        Printer.printGraphic(seats, tables);
        Printer.printPlayerWithIndex(curPlayer);
        break;
        case "take":
        curPlayer.takeCard();
        System.out.println();
        Printer.printPlayerWithIndex(curPlayer);
        break;
        case "discard":
        System.out.print("1 to " + curPlayer.getCards().size() + ": ");
        iCard = scanner.nextInt();
        curPlayer.selectCard(iCard - 1);
        curPlayer.layDownCard();
        System.out.println();
        Printer.printPlayerWithIndex(curPlayer);
        break;
        case "undo":
        curPlayer.takeBackCard();
        break;
        case "end":
        err = curPlayer.endTurn();
        Printer.printPlayerWithIndex(curPlayer);
        break;
        case "q":
        case "quit":
        break loop;
      }
      switch(err) {
        case ALONE:
        System.out.println("Der Gast braucht einen Gesprächspartner."); break;
        case TABLE_MISMATCH:
        System.out.println("Der Gast muss an einem Tisch seiner Nation sitzen."); break;
        case NO_CARD_LAID:
        System.out.println("Bitte mind. eine Karte legen."); break;
        case SEAT_IS_TAKEN:
        System.out.println("Der Platz ist bereits belegt."); break;
        case SEX_INEQUALITY:
        System.out.println("Die Geschlechterzahl ist nicht ausgeglichen."); break;
      }
      //System.out.println("");
    }
*/
    try {
      webSocket.close();
    } catch(IOException e) {}
  }

  public void end() {
    Printer.printRanking(curPlayer);
  }

  public void exchangeFullTables() {
    boolean[] fullTables = new boolean[5];

    for(int i = 0; i < 5; i++) {
      fullTables[i] = tables[i].isFull();
    }
    for(int i = 0; i < 5; i++) {
      if(fullTables[i]) {
        tables[i].empty();
        if(!tableStack.empty())
        tables[i].setNation(tableStack.pop());
        else
        end();
      }
    }

    if(specialMode == SpecialMode.FIRSTCARD) {
      specialMode = SpecialMode.SECONDCARD;
    } else if(specialMode == SpecialMode.SECONDCARD) {
      specialMode  = SpecialMode.NONE;
    } else {
      boolean noSeatTaken = true;
      for(Seat seat : seats) {
        if(seat.isTaken()) {
          noSeatTaken = false;
          break;
        }
      }
      if(noSeatTaken) {
        specialMode = SpecialMode.FIRSTCARD;
      }
    }
  }

  public Card popCard() {
    return stack.pop();
  }

  /*
  private void createSzenario() {
  tables[4].setNation(Nation.FRANCE);
  seats[1].set(new Card(Nation.GERMANY, Sex.MALE));
  seats[2].set(new Card(Nation.GERMANY, Sex.FEMALE));
  seats[0].set(new Card(Nation.GERMANY, Sex.FEMALE));
  seats[3].set(new Card(Nation.GERMANY, Sex.MALE));
  addPlayer("Line");
  addPlayer("Rubi");
  addPlayer("Linda");
  addPlayer("Steffen");
}

public void set(int seat, String nation, boolean female) {
Sex sex;
if(female) sex = Sex.FEMALE; else sex = Sex.MALE;
switch(nation) {
case "AF" :  seats[seat].set(new Card(Nation.AFRICA, sex)); break;
case "CN" :  seats[seat].set(new Card(Nation.CHINA, sex)); break;
case "CU" :  seats[seat].set(new Card(Nation.CUBA, sex)); break;
case "EN" :  seats[seat].set(new Card(Nation.ENGLAND, sex)); break;
case "FR" :  seats[seat].set(new Card(Nation.FRANCE, sex)); break;
case "DE" :  seats[seat].set(new Card(Nation.GERMANY, sex)); break;
case "IN" :  seats[seat].set(new Card(Nation.INDIA, sex)); break;
case "IT" :  seats[seat].set(new Card(Nation.ITALY, sex)); break;
case "RU" :  seats[seat].set(new Card(Nation.RUSSIA, sex)); break;
case "ES" :  seats[seat].set(new Card(Nation.SPAIN, sex)); break;
case "TR" :  seats[seat].set(new Card(Nation.TURKEY, sex)); break;
case "US" :  seats[seat].set(new Card(Nation.USA, sex)); break;
}
Printer.printGraphic(seats, tables);
}
*/
}
