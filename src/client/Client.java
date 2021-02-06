package client;

import programConstants.ProgramConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Set;

public class Client {

    InetSocketAddress serverAddress;
    SocketChannel toServer;
    ByteBuffer messageBuffer;  // the only buffer because we have only one channel to a server
    String nickname;

    public Client(String serverHost, int serverPort) throws UnknownHostException {
        this.serverAddress = new InetSocketAddress(serverHost, serverPort);
    }

    // a general method to run the client program and manage all events
    public void start() {
        try {
            //connecting to server
            toServer = SocketChannel.open(serverAddress);
            toServer.configureBlocking(false);

            if(toServer.isConnected()) {
                System.out.println(ProgramConstants.CONNECTED_SUCCESSFULLY);
            }

            // registering channel in selector
            Selector selector = Selector.open();
            messageBuffer = ByteBuffer.allocate(512);
            toServer.register(selector, SelectionKey.OP_WRITE, messageBuffer);

            // nickname registration
            nicknameRegistration();

            // infinite loop for the selector
            while(true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                // iterating over selected-keys set
                for(SelectionKey key : selectedKeys) {
                    if(key.isWritable()) {
                        sendToServer(key);
                    }
                }
                // it is important to clear the Set or program will reuse
                // selected keys that we already have - they keep staying there
                selectedKeys.clear();
            }
        } catch (IOException e) {
            System.out.println(ProgramConstants.SERVER_UNAVAILABLE);
        }
    }

    // this method sends special message to server that contains client's nickname to register in chat
    private void nicknameRegistration() throws IOException {
        System.out.print("Enter you nickname: ");
        Scanner scanner = new Scanner(System.in);
        nickname = scanner.nextLine();
        String registrationMessage = ProgramConstants.NICKNAME_REGISTRATION + nickname;
        byte[] registrationBytes = registrationMessage.getBytes();
        toServer.write(ByteBuffer.wrap(registrationBytes));
    }

    // a method to read incoming chat greeting - optional, doesn't execute in this version program
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
        if(!stringMessage.equals(ProgramConstants.DISCONNECT_USER)) {
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
        System.out.println(ProgramConstants.USER_DISCONNECTED);
        System.exit(0);
    }
}
