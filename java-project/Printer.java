package cafe;

import java.util.ArrayList;

public abstract class Printer {

    public static void printGraphic(Seat[] seats, Table[] tables) {
        System.out.println();
        System.out.println("---------------------------------------");
        System.out.println();

        print135(2, 4, -1, seats, tables);
        System.out.println("         -------           -------         ");
        print24(1, 3, 5, 0, 1, seats, tables);
        System.out.println("         -------  -------  -------         ");
        print135(0, 6, 4, seats, tables);
        System.out.println("         -------  -------  -------         ");
        print24(11, 9, 7, 3, 2, seats, tables);
        System.out.println("         -------           -------         ");
        print135(10, 8, -1, seats, tables);
        System.out.println("");
    }

    private static void print135(int s1, int s2, int t1, Seat[] seats, Table[] tables) {
        Seat seat1 = seats[s1];
        Seat seat2 = seats[s2];
        Table table1;

        StringBuffer line;
        line = new StringBuffer();
        line.insert(0, "                                             ");
        if(seat1.isTaken()) line.insert(9, seat1.getNation());
        if(t1 > -1) {
            line.insert(17, "|" + tables[t1].getNation());
            line.insert(25, "|");
        }
        if(seat2.isTaken()) line.insert(27, seat2.getNation());
        line.setLength(43);
        System.out.println(line);

        line.insert(0, "                                             ");
        line.insert(9, getAbbBySex(seat1.getSex()));
        line.insert(14, String.format("%2d", s1));
        if(t1 > -1) {
            line.insert(17, "|");
            line.insert(24, t1 + "|");
        }
        line.insert(27, getAbbBySex(seat2.getSex()));
        line.insert(32, String.format("%2d", s2));
        line.setLength(43);
        System.out.println(line);
    }

    private static void print24(int s1, int s2, int s3, int t1, int t2, Seat[] seats, Table[] tables) {
        Seat seat1 = seats[s1];
        Seat seat2 = seats[s2];
        Seat seat3 = seats[s3];
        Table table1 = tables[t1];
        Table table2 = tables[t2];

        StringBuffer line;
        line = new StringBuffer();
        line.insert(0, "                                             ");
        if(seat1.isTaken()) line.insert(0, seat1.getNation());
        line.insert(8, "|" + table1.getNation());
        line.insert(16, "| ");
        if(seat2.isTaken()) line.insert(17,seat2.getNation());
        line.insert(26, "|" + table2.getNation());
        line.insert(34, "| ");
        if(seat3.isTaken()) line.insert(35, seat3.getNation());
        line.setLength(43);
        System.out.println(line);

        line.insert(0, "                                             ");
        line.insert(0, getAbbBySex(seat1.getSex()));
        line.insert(5, String.format("%2d", s1));
        line.insert(8, "|");
        line.insert(15, t1);
        line.insert(16, "| " + getAbbBySex(seat2.getSex()));
        line.insert(23, String.format("%2d", s2));
        line.insert(26, "|");
        line.insert(33, t2);
        line.insert(34, "| " + getAbbBySex(seat3.getSex()));
        line.insert(41, String.format("%2d", s3));
        line.setLength(43);
        System.out.println(line);
    }

    private static String getAbbBySex(Sex pSex) {
        if(pSex == Sex.MALE)
            return "m";
        else if(pSex == Sex.FEMALE)
            return "f";
        else
            return "";
    }

    public static void printText(Seat[] seats, Table[] tables) {
        for(int i = 0; i < 12; i++) {
            System.out.print(seats[i].getNation() + " " + seats[i].getSex() + ";");
        }
        System.out.println();
        for(int i = 0; i < 5; i++) {
            System.out.print(tables[i].getNation() + ";");
        }
        System.out.println();
        System.out.println();
    }

    public static void printPlayer(Player player) {
        System.out.print(player.getName() + ":\t");
        for(Card card : player.getCards()) {
            System.out.print(card.getNation() + " " + getAbbBySex(card.getSex()) + ", ");
        }
        System.out.println();
    }

    public static void printPlayerWithIndex(Player player) {
        System.out.print(player.getName() + ":\t");
        int i = 1;
        for(Card card : player.getCards()) {
            System.out.print((i++) + " " + card.getNation() + " " + getAbbBySex(card.getSex()) + ", ");
        }
        System.out.println();
    }

    public static void printPlayers(Player firstPlayer) {
        Player curPlayer = firstPlayer;

        System.out.println();
        System.out.println();
        do {
            printPlayer(curPlayer);
            curPlayer = curPlayer.getNext();
        } while(curPlayer != firstPlayer);
    }

    public static void printRanking(Player firstPlayer) {
        Player curPlayer = firstPlayer;

        System.out.println();
        System.out.println();
        do {
            System.out.println(curPlayer.getName() + ":\t" + curPlayer.getPoints() + " Punkte");
            curPlayer = curPlayer.getNext();
        } while(curPlayer != firstPlayer);
    }
}
