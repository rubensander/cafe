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
    public ErrType isValidMove(Card pCard, SpecialMode pMode) {
        if(taken) {
            return ErrType.SEAT_IS_TAKEN;
        }
        if(table.getNation() != pCard.getNation() && table2.getNation() != pCard.getNation() && table3.getNation() != pCard.getNation()) {
            return ErrType.TABLE_MISMATCH;
        }
        if(pMode != SpecialMode.FIRSTCARD) {
            if(table.isEmpty() && table2.isEmpty() && table3.isEmpty()) {
                return ErrType.ALONE;
            }
            if(table.getSexMajorityCount(pCard.getSex()) > 0 || table2.getSexMajorityCount(pCard.getSex()) > 0 || table3.getSexMajorityCount(pCard.getSex()) > 0) {
                return ErrType.SEX_INEQUALITY;
            }
        }
        return ErrType.NONE;
    }

    @Override
    public int getPoints() {
        return table.getPoints() + table2.getPoints() + table3.getPoints();
    }
}
