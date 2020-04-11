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

    public int getSexMajorityCount(Sex pSex) {
        int count = 0;
        for(Seat seat : seats) {
            if(seat.isTaken()) {
                if(seat.getSex() == pSex)
                    count++;
                else
                    count--;
            }
        }
        return count;
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

        for(Seat seat : seats) {
            if(seat.isTaken()) {
                points++;
                if(seat.getNation() != nation)
                    oneNation = false;
            }
        }

        if(points == 1)
            return 0;
        else if(oneNation)
            return 2*points;
        else
            return points;
    }
}
