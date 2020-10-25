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
        } else if(table.getNation() != pCard.getNation() && table2.getNation() != pCard.getNation() && table3.getNation() != pCard.getNation()) {
            return ErrType.TABLE_MISMATCH;
        } else if(pMode == SpecialMode.FIRSTCARD) {
        	return ErrType.NONE;
        } else if(table.isEmpty() && table2.isEmpty() && table3.isEmpty()) {
            return ErrType.ALONE;
        }
        ErrType[] canSit = {table.canSit(pCard.getSex()), table2.canSit(pCard.getSex()), table3.canSit(pCard.getSex())};
        boolean onlyInCircle = false;
        for(int i = 0; i <= 2; i++) {
        	if(canSit[i] == ErrType.SEX_INEQUALITY) return ErrType.SEX_INEQUALITY;
        	if(canSit[i] == ErrType.ONLY_IN_CIRCLE) {
        		if(!onlyInCircle) 
        			onlyInCircle = true;
        		else
        			return ErrType.SEX_INEQUALITY;
        	}
        }
        if(onlyInCircle) return ErrType.ONLY_IN_CIRCLE;
        return ErrType.NONE;
    }

    @Override
    public int getPoints() {
        return table.getPoints() + table2.getPoints() + table3.getPoints();
    }
}
