package model;

public class RoomType {

    private final BedType bed;
    private final int price;
    private final int numberOfBeds;

    public RoomType(BedType bed, int price, int numberOfBeds) {
        this.bed = bed;
        this.price = price;
        this.numberOfBeds = numberOfBeds;
    }

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public BedType getBedType() {
        return bed;
    }

    public int getPrice() {
        return price;
    }

    public int getSpace() {
        return bed.getCapacity() * numberOfBeds;
    }

    public String toString() {
        return bed.name();
    }
}
