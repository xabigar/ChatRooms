package tedsu.um.chatrooms.messages.events;

//Contains the new location and the new routing keys, which are send when the application receives a new location.
public class LocationChangeEvent {
    String building;
    String floor;
    String room;
    String senderRoutingKey;
    String receiveRoutingKey;


    public LocationChangeEvent(String building, String floor, String room, String senderRoutingKey, String receiverRoutingKey) {
        this.building = building;
        this.floor = floor;
        this.room = room;
        this.senderRoutingKey = senderRoutingKey;
        this.receiveRoutingKey = receiverRoutingKey;
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

    public String getSenderRoutingKey() { return senderRoutingKey; }

    public String getReceiverRoutingKey() { return receiveRoutingKey; }
}
