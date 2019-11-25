package tedsu.um.chatrooms.messages.events;

public class LocationChangeEvent {
    String building;
    String floor;
    String room;

    public LocationChangeEvent(String building, String floor, String room) {
        this.building = building;
        this.floor = floor;
        this.room = room;
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
}
