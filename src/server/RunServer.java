package server;
import programConstants.ProgramConstants;

public class RunServer {

    public static void main(String[] args) {
        new Server(ProgramConstants.SERVER_HOST, ProgramConstants.SERVER_PORT).start();
    }

}
