package client;
import programConstants.ProgramConstants;

import java.net.UnknownHostException;

public class RunClient {
    public static void main(String[] args) {
        try {
            new Client(ProgramConstants.SERVER_HOST, ProgramConstants.SERVER_PORT).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
