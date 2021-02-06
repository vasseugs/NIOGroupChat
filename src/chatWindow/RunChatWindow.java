package chatWindow;
import programConstants.ProgramConstants;

import java.net.UnknownHostException;

public class RunChatWindow {
    public static void main(String[] args) {
        try {
            new ChatWindow(ProgramConstants.SERVER_HOST, ProgramConstants.SERVER_PORT).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
