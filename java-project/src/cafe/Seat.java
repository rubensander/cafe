package cafe;

public class Seat {
    boolean taken;
    Table table;
    Card card;

    public Seat(Table pTable) {
        table = pTable;
        taken = false;
        table.registerSeat(this);
    }

    public void set(Card pCard) {
        card = pCard;
        taken = true;
        table.set(pCard.getSex());
    }

    public ErrType isValidMove(Card pCard, SpecialMode pMode) {
        Sex sex = pCard.getSex();
        if(taken) {
            return ErrType.SEAT_IS_TAKEN;
        } else if(table.getNation() != pCard.getNation()) {
            return ErrType.TABLE_MISMATCH;
        } else if(pMode == SpecialMode.FIRSTCARD) {
            return ErrType.NONE;
        } else if(pMode == SpecialMode.CIRCLE) {
            if(table.isCircle) {
                if(table.getBalance(sex) < 0)
                    return ErrType.CIRCLE_WRONG_SEX;
                else
                    return ErrType.NONE;
            } else {
                return ErrType.CIRCLE_WRONG_TABLE;
            }
        } else if(table.isEmpty()) {
            return ErrType.ALONE;
        } else if(table.getBalance(sex) > 0) {
            return ErrType.SEX_IMBALANCE;
        } else {
            return ErrType.NONE;
        }
    }

    public Card empty() {
        Card temp = card;
        card = null;
        taken = false;
        return temp;
    }

    public Nation getNation() {
        if(card == null)
            return null;
        else
            return card.getNation();
    }

    public Sex getSex() {
        if(card == null)
            return null;
        else
            return card.getSex();
    }

    public boolean isTaken() {
        return taken;
    }

    public int getPoints() {
        return table.getPoints();
    }
/*
    public boolean areAdjacentSeats(Seat[] seats) {
        return table.areMySeats(seats);
    }
*/
}
