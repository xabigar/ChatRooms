package tedsu.um.chatrooms.messages.events;

public class LocationChangeEvent {
    String building;
    String floor;
    String room;
    String s;
    String r;


    public LocationChangeEvent(String building, String floor, String room, String s, String r) {
        this.building = building;
        this.floor = floor;
        this.room = room;
        this.s = s;
        this.r = r;
    }

    public String getBuilding() {
        return building;
    }

    public String getFloor() {
        return floor;
    }

    public String getRoom() {
        return room;
    }

    public String getS() { return s; }

    public String getR() { return r; }
}
