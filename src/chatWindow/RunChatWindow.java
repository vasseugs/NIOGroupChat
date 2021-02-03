package chatWindow;
import programConstants.ProgramConstants;

public class RunChatWindow {
    public static void main(String[] args) {
        new ChatWindow(ProgramConstants.SERVER_HOST, ProgramConstants.SERVER_PORT).start();
    }

}
