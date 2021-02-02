package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Set;

public class Client {

    InetSocketAddress serverAddress;
    SocketChannel toServer;
    ByteBuffer messageBuffer;

    public Client(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
    }

    public void start() {
        try {
            //connecting to server
            toServer = SocketChannel.open(serverAddress);
            toServer.configureBlocking(false);

            if(toServer.isConnected()) {
                System.out.println("Connected to server.");
            }

            Selector selector = Selector.open();
            messageBuffer = ByteBuffer.allocate(512);
            /* register the channel for reading first
            to read a greeting from server
             */
            toServer.register(selector, SelectionKey.OP_READ, messageBuffer);

            // infinite loop for the selector
            while(true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                // iterating over selected-keys set
                for(SelectionKey key : selectedKeys) {

                    if(key.isWritable()) {
                        sendToServer(key);
                    }

                    if(key.isReadable()) {
                        readFromServer(key);
                    }
                }
                // it is important to clear the Set or program will reuse
                // selected keys that we already have - they keep staying there
                selectedKeys.clear();
            }
        } catch (IOException e) {
            System.out.println("Сервер недоступен. Попробуйте подключиться позже.");
        }
    }

    // a method to read incoming chat greeting
    private void readFromServer(SelectionKey key) throws IOException {
        toServer = (SocketChannel) key.channel();
        messageBuffer = (ByteBuffer) key.attachment();

        /* read greeting from server. From now on, client
        will not read any incoming data anymore */
        toServer.read(messageBuffer);

        messageBuffer.flip();
        String message = new String(messageBuffer.array(), messageBuffer.position(), messageBuffer.limit());
        System.out.println(message);

        messageBuffer.clear();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    // general method for sending messages
    private void sendToServer(SelectionKey key) throws IOException {
        toServer = (SocketChannel) key.channel();
        messageBuffer = (ByteBuffer) key.attachment();

        // creating a message
        System.out.print("Me: ");
        Scanner console = new Scanner(System.in);

        String stringMessage = console.nextLine();
        byte[] byteMessage = stringMessage.getBytes();

        /* if the message does not contain quit command
        then we send it to server */
        if(!stringMessage.equals("quit")) {
            messageBuffer = messageBuffer.put(0, byteMessage);
            messageBuffer.limit(byteMessage.length);
            toServer.write(messageBuffer);
            messageBuffer.clear();
        }
        // otherwise we exit the client program
        else {
            disconnect(toServer);
        }
    }

    // a method to disconnect from server and exit the program
    private void disconnect(SocketChannel toServer) throws IOException {
        toServer.close();
        System.out.println("Disconnected from server.");
        System.exit(0);
    }
}
