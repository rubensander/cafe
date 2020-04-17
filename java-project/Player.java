package cafe;

import java.lang.Runnable;
import java.net.Socket;
import java.util.ArrayList;

public class Player implements Runnable {
    private int points;
    private ArrayList<Card> cards;
    private Player next;
    private String name;
    private int selectedCard;
    private int status;
    private Game game;
    private Socket socket;

    public Player(Game pGame, ArrayList<Card> startCards, Socket pSocket) {
        game = pGame;

        cards = new ArrayList<Card>(startCards);

        socket = pSocket;

        next = this;
        name = "";
        selectedCard = -1;
        status = -1;
    }

    public void run() {
      
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
