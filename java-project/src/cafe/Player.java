package cafe;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.Runnable;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class Player implements Runnable {
    private int points;
    private ArrayList<Card> cards;
    private Player next;
    private String name;
    private int selectedCard;
    private int status;
    private Game game;
    private Socket socket;

    public Player(Game pGame, Socket pSocket) throws IOException, NoSuchAlgorithmException {
        game = pGame;

        next = this;
        name = "";
        selectedCard = -1;
        status = -1;

        socket = pSocket;
        
        // do handshake
        InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
		Scanner s = new Scanner(in, "UTF-8");
		
		try {
			String data = s.useDelimiter("\\r\\n\\r\\n").next();
			Matcher get = Pattern.compile("^GET").matcher(data);
			
			if (get.find()) {
				Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
				match.find();
				byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
					+ "Connection: Upgrade\r\n"
					+ "Upgrade: websocket\r\n"
					+ "Sec-WebSocket-Accept: "
					+ Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
					+ "\r\n\r\n").getBytes("UTF-8");
				out.write(response, 0, response.length);
				byte[] decoded = new byte[6];
				byte[] encoded = new byte[] { (byte) 198, (byte) 131, (byte) 130, (byte) 182, (byte) 194, (byte) 135 };
				byte[] key = new byte[] { (byte) 167, (byte) 225, (byte) 225, (byte) 210 };
				for (int i = 0; i < encoded.length; i++) {
					decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
				}
			}
		} finally {
			s.close();
		}
    }

    public void run() {
      if(socket != null) {
    	InputStream in = null;
    	Scanner s = null;
        try {
          in = socket.getInputStream();
          //OutputStream out = socket.getOutputStream();
          s = new Scanner(in, "UTF-8");

          JSONObject msgObj = new JSONObject(s.nextLine());

          if(msgObj.get("status").equals("JOIN")) {
            if(msgObj.get("name") != "")
              cards = game.join(this);
            else
              throw new ProtocolException("Empty name.");
          }
          msgObj = new JSONObject(s.nextLine());
          if(msgObj.get("status").equals("START")) {
            if(game.specialMode == SpecialMode.NOTSTARTED)
              game.start();
            else
              throw new ProtocolException("Game has already been started.");
              msgObj = new JSONObject(s.nextLine());
          }

          // ...

        } catch(JSONException e) {
          System.out.println(name + ": Error with received JSON object. " + e.getMessage());
        } catch(ProtocolException e) {
          System.out.println(name + ": Error with protocol. " + e.getMessage());
        } catch(IOException e) {
          System.out.println(name + ": IO receiving Error. " + e.getMessage());
        } finally {
        	if(s != null) {
        		s.close();
        	}
        }
      }
    }

    public class ProtocolException extends Exception {
      public ProtocolException(String message) {
        super(message);
      }
    }

    public void send(String message) {
      try {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
        out.write(message);
        out.flush();
      } catch(IOException e) {
        System.out.println(name + ": IO sending Error. " + e.getMessage());
      }

    }

    public void setNext(Player pNext) {
        next = pNext;
    }

    public Player getNext() {
        return next;
    }

    public boolean takeCard() {
        if(cards.size() < 12) {
            cards.add(game.popCard());
            status = 1;
            endTurn();
            return true;
        }
        return false;
    }

    public int getPoints() {
        return points;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void selectCard(int index) {
        if(index >= 0 && index < cards.size()) {
            selectedCard = index;
        }
    }

    public void layDownCard() {
        if(selectedCard != -1) {
            cards.remove(selectedCard);
            points -= 2;
            endTurn();
        }
    }

    public ErrType placeCardAt(Seat pSeat) {
        if(status == -1) {
            ErrType err = pSeat.isValidMove(cards.get(selectedCard), game.specialMode);
            if(err == ErrType.NONE) {
                pSeat.set(cards.get(selectedCard));
                cards.remove(selectedCard);
                selectedCard = -1;
                status++;
                points += pSeat.getPoints();
                game.exchangeFullTables();
                if(status == 3) endTurn();
				return ErrType.NONE;
            }
            return err;
        }
        return ErrType.SB_ELSES_TURN;
    }

    public ErrType placeCardAt(int index, Seat pSeat) {
        selectCard(index);
        return placeCardAt(pSeat);
    }

    public boolean takeBackCard() {
        if(game.specialMode == SpecialMode.SECONDCARD) {
            status = 0;
            for(Seat seat : game.seats) {
                if(seat.isTaken()) {
                   cards.add(seat.empty());
                   break;
                }
            }
            game.specialMode = SpecialMode.FIRSTCARD;
            return true;
        }
        return false;
    }

    public void beginTurn() {
        status = 0;
    }

    public ErrType endTurn() {
        if(game.specialMode == SpecialMode.SECONDCARD)
            return ErrType.ALONE;
        if(status == 0)
            return ErrType.NO_CARD_LAID;
        status = -1;
        game.curPlayer = next;
        next.beginTurn();
        return ErrType.NONE;
    }
}
