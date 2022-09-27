package cafe;

public class Table {
    Seat[] seats;
    Nation nation;

    public Table(Nation pNation) {
        nation = pNation;
        seats = new Seat[4];
    }

    public void registerSeat(Seat pSeat) {
        for(int i = 0; i < 4; i++) {
            if(seats[i] == null) {
                seats[i] = pSeat;
                break;
            }
        }
    }

//     public int getSeatsCount(Nation pNation) {
//         int count = 0;
//         for(Seat seat : seats) {
//             if(seat.getNation() == pNation)
//                 count++;
//         }
//         return count;
//     }

    public ErrType canSit(Sex pSex, SpecialMode pMode) {
        int countTaken = 0;
        int countBalance = 0;
        for(Seat seat : seats) {
            if(seat.isTaken()) {
            	countTaken++;
                if(seat.getSex() == pSex)
                	countBalance++;
                else
                	countBalance--;
            }
        }
        if(countTaken > 0) {
        	if(countTaken == countBalance) 
        		return ErrType.ONLY_IN_CIRCLE; 
        	else 
        		return ErrType.SEX_INEQUALITY;
        }
        return ErrType.NONE;
        /*
        if(countBalance <= 0 && pMode == SpecialMode.CIRCLE) return ErrType.INCOMPLETE_CIRCLE;
        if(countBalance <= 0) return ErrType.NONE;
        if(countTaken == 1 && pMode == SpecialMode.CIRCLE) return ErrType.SEX_INEQUALITY;
        if(countTaken == countBalance) return ErrType.ONLY_IN_CIRCLE;
        return ErrType.SEX_INEQUALITY;
        */
    }

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation pNation) {
        nation = pNation;
    }

    public boolean isFull() {
        for(Seat seat : seats) {
            if(!seat.isTaken()) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        for(Seat seat : seats) {
            if(seat.isTaken()) return false;
        }
        return true;
    }

    public void empty() {
        nation = null;
        for(Seat seat : seats){
            seat.empty();
        }
    }

    public int getPoints() {
        int points = 0;
        boolean oneNation = true;

        int countBalance = 0;
        for(Seat seat : seats) {
            if(seat.isTaken()) {
                points++;
                if(seat.getNation() != nation)
                    oneNation = false;
                if(seat.getSex() == Sex.f) countBalance++; else countBalance--;
            }
        }
        countBalance = Math.abs(countBalance);

        if(points == 1)
            return 0;
        else if(countBalance == 4) // is complete circle
        	return 40;
        else if(countBalance > 1) // else: is incomplete circle
        	return 0;
        else if(oneNation)
            return 2*points;
        else
            return points;
    }
}
