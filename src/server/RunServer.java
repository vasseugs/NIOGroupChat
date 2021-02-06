package server;
import programConstants.ProgramConstants;

import java.net.UnknownHostException;

public class RunServer {

    public static void main(String[] args) {
        try {
            new Server(ProgramConstants.SERVER_HOST, ProgramConstants.SERVER_PORT).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
