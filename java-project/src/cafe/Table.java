package cafe;

public class Table {
    Seat[] seats;
    Nation nation;
    public boolean isCircle;
    int countTaken;
    int countBalance; // f +, m -

    public Table(Nation pNation) {
        nation = pNation;
        seats = new Seat[4];
        isCircle = false;
        countTaken = 0;
        countBalance = 0;
    }

    public void registerSeat(Seat pSeat) {
        for(int i = 0; i < 4; i++) {
            if(seats[i] == null) {
                seats[i] = pSeat;
                break;
            }
        }
    }

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation pNation) {
        nation = pNation;
    }

    public boolean isFull() {
        return countTaken == 4;
    }

    public boolean isEmpty() {
        return countTaken == 0;
    }

    public void empty() {
        nation = null;
        countTaken = 0;
        countBalance = 0;
        for(Seat seat : seats){
            seat.empty();
        }
    }
/*
    public ErrType canSit(Sex pSex) {
        int relativeBalance = countBalance;
        if(pSex == Sex.m) relativeBalance = -1 * relativeBalance;

        if(isCircle && relativeBalance < 0) {
            return ErrType.CIRCLE_WRONG_SEX;
        } else if(relativeBalance >= 1) {
            return ErrType.SEX_INEQUALITY;
        }
        return ErrType.NONE;
    }
*/
    public int getPoints() {
        int points;
        
        if(countTaken == 1)
            points = 0;
        else if(Math.abs(countBalance) == 4)
        	points = 20;
        else
        	points = countTaken;

        if(isFull()) {
            for(Seat seat : seats) {
                if(seat.getNation() != nation) return points;
            }
        }
        return 2 * points;
    }
/*
    public boolean areMySeats(Seat[] pSeats) {
        for(Seat s1 : pSeats) {
            boolean isMySeat = false;
            for(Seat s2 : seats) {
                if(s1.equals(s2)) isMySeat = true;
            }
            if(!isMySeat) return false;
        }
        return true;
    }
*/
    public void set(Sex pSex) {
        countTaken++;
        if(pSex == Sex.f) countBalance++; else countBalance--;
    }

    public int getBalance(Sex pSex) {
        int relativeBalance = countBalance;
        if(pSex == Sex.m) relativeBalance = -1 * relativeBalance;
        return relativeBalance;
    }

    public boolean suitableForCircle() {
        if(isCircle) return false;
        return (countTaken == Math.abs(countBalance));
    }
}
