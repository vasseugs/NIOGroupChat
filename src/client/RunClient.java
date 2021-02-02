package client;

public class RunClient {
    public static void main(String[] args) {
        new Client("localhost", 31000).start();
    }

}
