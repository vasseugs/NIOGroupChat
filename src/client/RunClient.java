package client;
import programConstants.ProgramConstants;

public class RunClient {
    public static void main(String[] args) {
        new Client(ProgramConstants.SERVER_HOST, ProgramConstants.SERVER_PORT).start();
    }

}
