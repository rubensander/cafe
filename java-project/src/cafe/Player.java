package cafe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
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
	private InputStream in;
	private OutputStream out;

	public Player(Game pGame, Socket pSocket) throws IOException, NoSuchAlgorithmException {
		game = pGame;

		next = this;
		name = "";
		selectedCard = -1;
		status = -1;

		socket = pSocket;
		
		handshake();
	}
	
	private void handshake() throws IOException, NoSuchAlgorithmException {
		in = socket.getInputStream();
		out = socket.getOutputStream();
		
		@SuppressWarnings("resource")
		Scanner s = new Scanner(in, "UTF-8");

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
		}
	}

	public void run() {
		if(socket == null || socket.isClosed()) return;
		
		try {
			JSONObject msgObj = parseNextMessage();
			
			// if player was never before connected: get name
			if(name.isEmpty()) {
				if(msgObj.get("status").equals("JOIN")) {
					while(msgObj.getString("name").isEmpty()) {
						send(new JSONObject().put("status", "ERR").put("message", "Der Name darf nicht leer sein.").toString());
						msgObj = parseNextMessage();
					}
					name = msgObj.getString("name");
					cards = game.join(this);
				}
				
				msgObj = parseNextMessage();
				if(msgObj.get("status").equals("START")) {
					if(game.specialMode == SpecialMode.NOTSTARTED)
						game.start();
					msgObj = parseNextMessage();
				}
			}
			
			while(true) {
				try {
					if(msgObj.get("status").equals("SET_CARD")) {
						selectCard(msgObj.getInt("cardNr"));
						ErrType err = placeCardAt(game.getSeatByNr(msgObj.getInt("seatNr")));
						if(err == ErrType.NONE) {
							game.broadcastBoard();
						} else {
							JSONObject errObj = new JSONObject();
							errObj.put("status", "ERR");
							errObj.put("message", game.curPlayer.getName() + " ist am Zug!");
							send(errObj.toString());
						}
					} else if(msgObj.get("status").equals("GET_VALID_MOVES")) {
						int cardNr = msgObj.getInt("cardNr");
						JSONArray jsonValidMoves = new JSONArray();
						for(int iSeat = 0; iSeat < 12; iSeat++) {
							if(status == -1)
								jsonValidMoves.put(ErrType.SB_ELSES_TURN);
							else
								jsonValidMoves.put(game.getSeatByNr(iSeat).isValidMove(cards.get(cardNr), game.specialMode));
						}
						JSONObject resObj = new JSONObject().put("status", "VALID_MOVES");
						resObj.put("validMoves", jsonValidMoves);
						send(resObj.toString());
					} else {
						System.out.println(name + ": Unknown message: " + msgObj.toString());
					}
				} catch(JSONException e) {
					System.out.println(name + "JSON error. Message was not sent or processed.");
				}
				msgObj = parseNextMessage();
			}
		} catch(JSONException e) {
			System.out.println(name + ": JSON error. " + e.getMessage());
		//} catch(ProtocolException e) {
		//	System.out.println(name + ": Error with protocol. " + e.getMessage());
		} catch(IOException e) {
			System.out.println(name + ": IO receiving Error. " + e.getMessage());
		} catch(WebsocketException e) {
			if(e.opcode == 8) 
				System.out.println(name + ": Websocket closed by client. Payload: " + Arrays.toString(e.payload) + ".");
			else
				System.out.println(name + ": Websocket Error. Opcode: " + e.opcode + ". Length: " + e.length + ". Payload: " + Arrays.toString(e.payload) + ".");
		} finally {
			try {
				in.close();
				out.close();
				socket.close();
			} catch(IOException ex) {}
		}
	}
	
	public boolean offerReconnection(Socket newSocket) {
		String oldAddr = ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostName();
		String newAddr = ((InetSocketAddress) newSocket.getRemoteSocketAddress()).getHostName();
		System.out.println(name + ": " + socket.toString() + "->" + newSocket.toString());
		//TODO: cookie for session id?
		if(socket == null || socket.isClosed() && oldAddr.equals(newAddr)) {
			socket = newSocket;
			try {
				handshake();
				System.out.println(name + " reconnected successfully.");
				if(game.specialMode == SpecialMode.NOTSTARTED) {
					try {
						JSONObject msgObj = new JSONObject();
						
						if(next == this) 
							msgObj.put("enableStart", new Boolean(false));
						else
							msgObj.put("enableStart", new Boolean(true));
						
						// get player list (in correct order)
						JSONArray players = new JSONArray();
						Player p = game.curPlayer.getNext();
						do {
							players.put(p.getName());
							p = p.getNext();
						} while(p != game.curPlayer.getNext());
						msgObj.put("players", players);
						msgObj.put("status", "NEW_PLAYER");
						send(msgObj.toString());
					} catch(JSONException e) {
						
					}
				} else {
					send("{\"status\":\"STARTED\"}");
					sendHand();
					game.broadcastBoard();
					send("{\"status\":\"TURN_OF\", \"player\":\"" + game.curPlayer.getName() + "\", \"yourName\":\"" + name + "\"}");
				}
				return true;
				
			} catch(IOException | NoSuchAlgorithmException e) {
				System.out.println(name + ": Handshake failed." + e.getMessage());
			}
		}
		return false;
	}

	class ProtocolException extends Exception {
		private static final long serialVersionUID = 1L;

		public ProtocolException(String message) {
			super(message);
		}
	}

	public void send(String message) throws IOException {
		byte[] payload = message.getBytes();
		byte[] packet;
		int offset;
		if(payload.length < 126) {
			packet = new byte[payload.length + 2];
			packet[1] = (byte) payload.length;
			offset = 2;
		} else {
			packet = new byte[payload.length + 4];
			packet[1] = (byte) 126;
			packet[2] = (byte) (payload.length / 256);
			packet[3] = (byte) (payload.length % 256);
			offset = 4;
		}

		packet[0] = (byte) 129;

		for(int i = 0; i < payload.length; i++) {
			packet[i + offset] = payload[i];
		}

		//try {
			out.write(packet, 0, packet.length);
		//} catch(IOException e) {
		//	System.out.println(name + ": IO sending Error. " + e.getMessage());
		//}
	}

	public void sendHand() throws IOException {
		try {
			JSONObject data = new JSONObject();
			data.put("status", "HAND");
			JSONArray jsonCards = new JSONArray();
			for(Card card : cards) {
				jsonCards.put(card.getNation().toString() + "_" + card.getSex().toString());
			}
			data.put("cards", jsonCards);
			String message = data.toString();

			send(message);
		} catch(JSONException e) {
			System.out.println(name + ": JSON Error while sending hand. " + e.getMessage());
		}
	}

	private JSONObject parseNextMessage() throws IOException, WebsocketException {
		int status = in.read();
		if(status == 129 || status == 136) {
			int length = in.read() - 128; // subtract "mask" bit

			if(length < 126) {
				byte[] decoded = new byte[length];
				byte[] key = new byte[] { (byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read() };
				for(byte i = 0; i < length; i++) {
					decoded[i] = (byte) (in.read() ^ key[i & 0x3]);
				}

				if(status == 136)
					throw new WebsocketException(status, length, decoded);
				
				try {
					return new JSONObject(new String(decoded));
				} catch(JSONException ex) {
					return new JSONObject();
				}
			}
		}
		throw new WebsocketException(status, 0, new byte[0]);
	}

	public class WebsocketException extends Exception {
		private static final long serialVersionUID = 1L;
		public boolean fin;
		public int opcode;
		public int length;
		public byte[] payload;

		public WebsocketException(int status, int length, byte[] payload) {
			this.fin = (status / 128 != 0);
			this.opcode = status % 128;
			this.length = length;
			this.payload = payload;
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
		if(status != -1) {
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
		game.curPlayer = this;
		try {
			game.broadcast("TURN_OF", new JSONObject().put("player", name));
		} catch(JSONException e) {
			System.out.println(name + ": JSON Error while creating beginTurn JSONObject.");
		}
	}

	public ErrType endTurn() {
		if(game.specialMode == SpecialMode.SECONDCARD)
			return ErrType.ALONE;
		if(status == 0)
			return ErrType.NO_CARD_LAID;
		status = -1;
		next.beginTurn();
		return ErrType.NONE;
	}
}
