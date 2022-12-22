package cafe;

public class CenterSeat extends Seat {
    Table table2, table3;

    public CenterSeat(Table pTable1, Table pTable2, Table pTable3) {
        super(pTable1);

        table2 = pTable2;
        table2.registerSeat(this);

        table3 = pTable3;
        table3.registerSeat(this);
    }

    @Override
    public void set(Card pCard) {
        card = pCard;
        taken = true;
    }

    @Override
    public ErrType isValidMove(Card pCard, SpecialMode pMode) {
        Sex sex = pCard.getSex();
        if(taken) {
            return ErrType.SEAT_IS_TAKEN;
        } else if(table.getNation() != pCard.getNation() && table2.getNation() != pCard.getNation() && table3.getNation() != pCard.getNation()) {
            return ErrType.TABLE_MISMATCH;
        } else if(pMode == SpecialMode.FIRSTCARD) {
        	return ErrType.NONE;
        } else if(pMode == SpecialMode.CIRCLE) {
            if(table.isCircle) {
                if(table.getBalance(sex) < 0) return ErrType.CIRCLE_WRONG_SEX; else return ErrType.NONE;
            } else if(table2.isCircle) {
                if(table2.getBalance(sex) < 0) return ErrType.CIRCLE_WRONG_SEX; else return ErrType.NONE;
            } else if(table3.isCircle) {
                if(table3.getBalance(sex) < 0) return ErrType.CIRCLE_WRONG_SEX; else return ErrType.NONE;
            } else {
                return ErrType.CIRCLE_WRONG_TABLE;
            }
        } else if(table.isEmpty() && table2.isEmpty() && table3.isEmpty()) {
            return ErrType.ALONE;
        } else if((!table.isEmpty() && table.getBalance(sex) > 0) || (table2.isEmpty() && table2.getBalance(sex) > 0) || (table2.isEmpty() && table3.getBalance(sex) > 0)) {
            return ErrType.SEX_IMBALANCE;
        }
        return ErrType.NONE;
    }

    @Override
    public int getPoints() {
        return table.getPoints() + table2.getPoints() + table3.getPoints();
    }
/*
    @Override
    public boolean areAdjacentSeats(Seat[] seats) {
        return (table.areMySeats(seats) || table2.areMySeats(seats) || table3.areMySeats(seats));
    }
*/
}
