package org.pepfar.pdma.app.data.types;

public enum TBProphylaxisRegimen {

    _6H(0),

    _9H(1),

    _3HP(2),

    _3RH(3),

    _4R(4),

    _1HP(5),

    OTHER(6);

    private final int number;

    private TBProphylaxisRegimen(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        switch (this) {
            case _6H:
                return "6H";
            case _9H:
                return "9H";
            case _1HP:
                return "1HP";
            case _3HP:
                return "3HP";
            case _3RH:
                return "3RH";
            case _4R:
                return "4R";
            default:
                return "PĐ khác";
        }
    }
}
