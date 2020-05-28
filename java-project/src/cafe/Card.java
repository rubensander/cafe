package cafe;

public class Card {
    private Nation nation;
    private Sex sex;

    public Card(Nation pNation, Sex pSex) {
        nation = pNation;
        sex = pSex;
    }

    public Nation getNation() {
        return nation;
    }

    public Sex getSex() {
        return sex;
    }
}
