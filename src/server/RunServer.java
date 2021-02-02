package server;

public class RunServer {

    public static void main(String[] args) {
        new Server("localhost", 31000).start();
    }

}
