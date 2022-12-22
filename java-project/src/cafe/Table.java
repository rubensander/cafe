package cafe;

public class Table {
    Seat[] seats;
    Nation nation;
    public boolean isCircle;

    public Table(Nation pNation) {
        nation = pNation;
        seats = new Seat[4];
        isCircle = false;
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
        return countTaken() == 4;
    }

    public boolean isEmpty() {
        return countTaken() == 0;
    }

    public void empty() {
        nation = null;
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
        int points = countTaken();
        
        if(points == 1)
            points = 0;
        else if(Math.abs(getBalance(Sex.f)) == 4)
        	points = 20;

        for(Seat seat : seats) {
            if(seat.getNation() != nation) return points;
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

    int countTaken() {
        int taken = 0;
        for(Seat seat : seats) {
            if(seat.taken) taken++;
        }
        return taken;
    }

    public int getBalance(Sex pSex) {
        int balance = 0;
        for(Seat seat : seats) {
            if(seat.getSex() != null) {
                if(seat.getSex() == pSex)
                    balance++;
                else
                    balance--;
            }
        }
        return balance;
    }

    public boolean suitableForCircle() {
        if(isCircle) return false;
        Sex sex = null;
        for(Seat seat : seats) {
            if(sex == null)
                sex = seat.getSex();
            else if(seat.getSex() != null && seat.getSex() != sex)
                return false;
        }
        return true;
    }
}
