package tedsu.um.chatrooms.messages.actions;

public class ChatNewMessageAction {
    String message;
    String origin;

    public ChatNewMessageAction(String message, String origin) {
        this.message = message;
        this.origin = origin;
    }

    public String getMessage() {
        return message;
    }
}
