package model;

public enum BedType {

    TWIN(1),
    TWINXL(1),
    FULL(1),
    QUEEN(2),
    KING(2);

    private final int capacity;

    BedType(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}
