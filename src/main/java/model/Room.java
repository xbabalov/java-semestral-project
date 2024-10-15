package model;

public class Room {

    private Long id;
    private final int number;
    private final RoomType type;

    public Room(int number, RoomType type) {
        this.number = number;
        this.type = type;
    }

    public String toString() {
        return String.valueOf(number);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return type.getSpace();
    }

    public RoomType getType() {
        return type;
    }
}
