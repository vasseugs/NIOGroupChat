package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Server {

    Selector selector;
    InetSocketAddress serverAddress;
    InetSocketAddress chatWindowAddress;
    ServerSocketChannel serverSocketChannel;
    SocketChannel chatWindow;

    public Server(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
        chatWindowAddress = new InetSocketAddress("localhost", 46000);
    }

    // a method to start server and transfer incoming messages to chatWindow
    public void start() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(serverAddress);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started.");

            while(true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for(SelectionKey key : selectedKeys) {
                    if(key.isAcceptable()) {
                        accept(key);
                    }

                    if(key.isReadable()) {
                        try {
                            handleMessage(key);
                        } catch (SocketException e) {
                            deleteUser(key);
                        }
                    }
                }
                selectedKeys.clear();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // new client accepting procedure
    private void accept(SelectionKey key) throws IOException {
        serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel newClient = serverSocketChannel.accept();
        newClient.configureBlocking(false);
        newClient.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(512));

        ByteBuffer buffer = ByteBuffer.wrap("You have connected the chat!".getBytes());
        newClient.write(buffer);

        printToChat("New user " + newClient.socket().getRemoteSocketAddress() + " connected to chat!");
    }

    // a method to receive a message from a client and print in chat for all
    private void handleMessage(SelectionKey key) throws IOException {
        SocketChannel fromClient = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        int bytesInMessage = fromClient.read(buffer);
        buffer.flip();

        String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();
        // if the client doesn't want to disconnect, print his message in chat
        if(bytesInMessage != -1) {
            printToChat(fromClient.socket().getRemoteSocketAddress() + ": " + clientMessage);
        } else {
            deleteUser(key);
        }

    }

    // a method to delete user from chat
    private void deleteUser(SelectionKey key) throws IOException {
        SocketChannel user = (SocketChannel) key.channel();
        user.close();
        key.cancel();
        printToChat("User " + user.socket().getRemoteSocketAddress() + " left the chat");
    }

    // a method to print any kind of messages in chat
    private void printToChat(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        chatWindow = SocketChannel.open(chatWindowAddress);
        chatWindow.write(buffer);
        buffer.flip();
    }

}
