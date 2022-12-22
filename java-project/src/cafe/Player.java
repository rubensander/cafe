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
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Player {
	private int points;
	private ArrayList<Card> cards;
	private Player next;
	private String name;
	private int status;
	private Game game;
	private Stack<Seat> laidUnderReserve;
	private PlayerSocket socket;

	public Player(Game pGame, Socket pSocket) throws IOException, NoSuchAlgorithmException {
		game = pGame;

		next = this;
		name = "";
		status = -1;
		laidUnderReserve = new Stack<Seat>();

		socket = new PlayerSocket(pSocket);
	}

	
	public class PlayerSocket implements Runnable {
		private Socket socket;
		private InputStream in;
		private OutputStream out;

		public PlayerSocket(Socket pSocket) throws IOException, NoSuchAlgorithmException {
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
					if(msgObj.get("msgType").equals("JOIN")) {
						while(msgObj.getString("name").isEmpty()) {
							send(new JSONObject().put("msgType", "ERR").put("message", "Der Name darf nicht leer sein.").toString());
							msgObj = parseNextMessage();
						}
						name = msgObj.getString("name");
						cards = game.join(Player.this);
					}
					
					msgObj = parseNextMessage();
					if(msgObj.get("msgType").equals("START")) {
						if(game.specialMode == SpecialMode.NOTSTARTED)
							game.start();
						msgObj = parseNextMessage();
					}
				}
				
				while(true) {
					try {
						if(msgObj.get("msgType").equals("SET_CARD")) {
							if(status > -1) {
								placeCardAt(game.getSeatByNr(msgObj.getInt("seatNr")), msgObj.getInt("cardNr"));
								sendInfo();
								if(game.specialMode == SpecialMode.ENDED)
									break;
								else
									game.broadcastBoard();
							} else {
								sendErr(game.curPlayer.getName() + " ist am Zug!");
							}
						}
						else if(msgObj.get("msgType").equals("GET_VALID_MOVES")) {
							Card card = cards.get(msgObj.getInt("cardNr"));
							JSONArray jsonValidMoves = new JSONArray();
							for(int iSeat = 0; iSeat < 12; iSeat++)
								jsonValidMoves.put(game.getSeatByNr(iSeat).isValidMove(card, game.specialMode));
							JSONObject resObj = new JSONObject().put("msgType", "VALID_MOVES");
							resObj.put("validMoves", jsonValidMoves);
							send(resObj.toString());
						}
						else if(msgObj.get("msgType").equals("DRAW")) { 
							ErrType err = drawCard();
							if(err == ErrType.NONE) {
								Card card = cards.get(cards.size() - 1);
								JSONObject resObj = new JSONObject().put("msgType", "DRAWN");
								resObj.put("card", card.getNation().toString() + "_" + card.getSex().toString());
								send(resObj.toString());
							} else
								sendErr(err.toString());
						}
						else if(msgObj.get("msgType").equals("END_TURN")) {
							ErrType err = endTurn();
							if(err == ErrType.NONE)
								game.broadcastBoard();
							else
								sendErr(err.toString());
						}
						else if(msgObj.get("msgType").equals("TAKE_BACK_CARD")) {
							Card takenBackCard = takeBackCard();
							if(takenBackCard != null) {
								game.broadcastBoard();
								JSONObject resObj = new JSONObject().put("msgType", "DRAWN");
								resObj.put("card", takenBackCard.getNation().toString() + "_" + takenBackCard.getSex().toString());
								send(resObj.toString());
								sendInfo();
							}
						}
						else if(msgObj.get("msgType").equals("CIRCLE")) {
							ErrType err = ErrType.NONE;
							if(msgObj.has("tableNr")) {
								err = startCircle(msgObj.getInt("tableNr"));
							} else if(msgObj.has("cancel")) {
								err = cancelCircle();
							} else if(msgObj.has("info")) {
								JSONArray jsonSuitableTables = new JSONArray();
								for(int iTable = 0; iTable < 5; iTable++)
									jsonSuitableTables.put(game.getTableByNr(iTable).suitableForCircle());
								JSONObject resObj = new JSONObject().put("msgType", "CIRCLE");
								resObj.put("suitableTables", jsonSuitableTables);
								send(resObj.toString());
							}
							if(err != ErrType.NONE) sendErr(err.toString());
						}
						else if(msgObj.get("msgType").equals("GAME_ENDED"))
							break;
						else {
							System.out.println(name + ": Unknown message: " + msgObj.toString());
						}
					} catch(JSONException e) {
						System.out.println(name + "JSON error. Message was not sent or processed.");
					}
					msgObj = parseNextMessage();
				}
			} catch(JSONException e) {
				System.out.println(name + ": JSON error. " + e.getMessage());
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
							
							if(next == Player.this) 
								msgObj.put("enableStart", Boolean.FALSE);
							else
								msgObj.put("enableStart", Boolean.TRUE);
							
							// get player list (in correct order)
							JSONArray players = new JSONArray();
							Player p = game.curPlayer.getNext();
							do {
								players.put(p.getName());
								p = p.getNext();
							} while(p != game.curPlayer.getNext());
							msgObj.put("players", players);
							msgObj.put("msgType", "NEW_PLAYER");
							send(msgObj.toString());
						} catch(JSONException e) {
							
						}
					} else {
						send("{\"msgType\":\"STARTED\"}");
						sendHand();
						game.broadcastBoard();
						send("{\"msgType\":\"TURN_OF\", \"player\":\"" + game.curPlayer.getName() + "\", \"yourName\":\"" + name + "\"}");
					}
					return true;
					
				} catch(IOException | NoSuchAlgorithmException e) {
					System.out.println(name + ": Handshake failed." + e.getMessage());
				}
			}
			return false;
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
				data.put("msgType", "HAND");
				JSONArray jsonCards = new JSONArray();
				for(Card card : cards) {
					jsonCards.put(card.getNation().toString() + "_" + card.getSex().toString());
				}
				data.put("cards", jsonCards);
				data.put("canEndTurn", status > 0);
				data.put("points", points);
				String message = data.toString();
				send(message);
				
				sendInfo();
			} catch(JSONException e) {
				System.out.println(name + ": JSON Error while sending hand. " + e.getMessage());
			}
		}
		
		private void sendInfo() throws IOException, JSONException {
			JSONObject data = new JSONObject().put("msgType", "INFO");
			data.put("points", points);
			data.put("canTakeBackCard", false);
			data.put("canEndTurn", false);
			data.put("canStartCircle", false);
			data.put("isCircle", false);
			if(!laidUnderReserve.empty())
				data.put("canTakeBackCard", true);
			else if(status > 0 && game.specialMode != SpecialMode.ENDED)
				data.put("canEndTurn", true);
			else if(status == 0)
				data.put("canStartCircle", true);
			if(game.specialMode == SpecialMode.CIRCLE)
				data.put("isCircle", true);

			String message = data.toString();
			send(message);
		}
		
		private void sendErr(String message) throws IOException {
			try {
				JSONObject errObj = new JSONObject();
				errObj.put("msgType", "ERR");
				errObj.put("message", message);
				
				send(errObj.toString());
			} catch(JSONException e) {
				System.out.println(name + ": JSON Error while sending error message. " + e.getMessage());
			}
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
	}

	public String getName() {
		return name;
	}

	public Player getNext() {
		return next;
	}

	public int getPoints() {
		return points;
	}

	public void setNext(Player pNext) {
		next = pNext;
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

	private ErrType drawCard() {
		if(status == -1) 
			return ErrType.SB_ELSES_TURN;
		if(status > 0) 
			return ErrType.CARD_LAID;
		if(cards.size() >= 12)
			return ErrType.TOO_MANY_HANDCARDS;
		
		cards.add(game.popCard());
		status = -1;
		next.beginTurn();
		return ErrType.NONE;
	}

	/*
	private void layDownCard(int cardNr) {
		if(cardNr != -1) {
			cards.remove(cardNr);
			points -= 2;
			endTurn();
		}
	}
	*/

	private void placeCardAt(Seat pSeat, int cardNr) {
		pSeat.set(cards.get(cardNr));
		cards.remove(cardNr);
		status++;
		points += pSeat.getPoints();

		boolean fullCircle = false;
		Table table;
		for(int iTable = 0; iTable < 5; iTable++) {
			table = game.getTableByNr(iTable);
			fullCircle |= (table.isCircle && table.isFull());
		}

		if (game.specialMode == SpecialMode.FIRSTCARD) {
			laidUnderReserve.push(pSeat);
			game.specialMode = SpecialMode.SECONDCARD;
		} else if(game.specialMode == SpecialMode.CIRCLE && status < 4 && !fullCircle) {
			laidUnderReserve.push(pSeat);
		} else {
			game.specialMode = SpecialMode.NONE;
			game.exchangeFullTables();
			laidUnderReserve.clear();
			if(game.specialMode != SpecialMode.ENDED && status >= 3)
				endTurn();
		}
	}

	private Card takeBackCard() {
		Card cardTakenBack = null;
		if(game.specialMode == SpecialMode.SECONDCARD || game.specialMode == SpecialMode.CIRCLE) {
			Seat seat = laidUnderReserve.pop();
			points -= seat.getPoints();
			cardTakenBack = seat.empty();
			cards.add(cardTakenBack);
			status--;
			if(game.specialMode == SpecialMode.SECONDCARD) 
				game.specialMode = SpecialMode.FIRSTCARD;
			else if(laidUnderReserve.isEmpty()) { // && CIRCLE
				game.specialMode = SpecialMode.NONE;
			}
		}
		return cardTakenBack;
	}

	private ErrType endTurn() {
		if(status == -1) 
			return ErrType.SB_ELSES_TURN;
		if(game.specialMode == SpecialMode.SECONDCARD)
			return ErrType.ALONE;
		if(game.specialMode == SpecialMode.CIRCLE)
			return ErrType.CIRCLE_INCOMPLETE;
		if(status == 0)
			return ErrType.NO_CARD_LAID;
		status = -1;
		next.beginTurn();
		return ErrType.NONE;
	}

	public PlayerSocket getSocket() {
		return socket;
	}

	private ErrType startCircle(int tableNr) {
		if(status == -1) 
			return ErrType.SB_ELSES_TURN;
		if(status > 0) 
			return ErrType.CARD_LAID;
		if(game.specialMode == SpecialMode.CIRCLE)
			return ErrType.CIRCLE_INCOMPLETE;
		
		Table table = game.getTableByNr(tableNr);
		if(table.suitableForCircle()) {
			game.specialMode = SpecialMode.CIRCLE;
			table.isCircle = true;
			return ErrType.NONE;
		} else {
			return ErrType.CIRCLE_UNSUITABLE_TABLE;
		}
		
	}

	private ErrType cancelCircle() {
		if(status == -1) 
			return ErrType.SB_ELSES_TURN;
		if(status > 0) 
			return ErrType.CARD_LAID;

		for(int iTable = 0; iTable < 5; iTable++) {
			game.getTableByNr(iTable).isCircle = false;
		}
		int seatsTaken = 0;
		for(int iSeat = 0; iSeat < 12; iSeat++) {
			if(game.getSeatByNr(iSeat).isTaken()) seatsTaken++;
		}
		if(seatsTaken == 0) 
			game.specialMode = SpecialMode.FIRSTCARD;
		else if(seatsTaken == 1)
			game.specialMode = SpecialMode.SECONDCARD;
		else
			game.specialMode = SpecialMode.NONE;
		return ErrType.NONE;
	}
}
