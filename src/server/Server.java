package server;

import programConstants.ProgramConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    Selector selector;
    InetSocketAddress serverAddress;
    ServerSocketChannel serverSocketChannel;

    HashSet<SocketChannel> attachedChatWindows;     // container for channels that represent chat windows
    HashMap<SocketChannel, String> userNicknames;

    public Server(String host, int port) throws UnknownHostException {
        this.serverAddress = new InetSocketAddress(host, port);
        attachedChatWindows = new HashSet<>();
        userNicknames = new HashMap<>();
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

    // general method to accept incoming connections
    private void accept(SelectionKey key) throws IOException {
        serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel newClient = serverSocketChannel.accept();
        newClient.configureBlocking(false);
        newClient.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(512));
    }

    // a method to receive a message from a client and print in chat for all
    private void handleMessage(SelectionKey key) throws IOException {
        SocketChannel fromClient = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        // forming a message
        int bytesInMessage = fromClient.read(buffer);
        buffer.flip();
        String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();

        /* if a message contains chat window marker,
        we treat its channel as a chat window and add it  to chat windows collection.
        otherwise its channel comes from a client and hence
        we register his nickname and then start printing his messages to a chat
         */
        Pattern pattern = Pattern.compile(ProgramConstants.NICKNAME_REGISTRATION);
        Matcher matcher = pattern.matcher(clientMessage);

        if(clientMessage.equals(ProgramConstants.CHAT_WINDOW_MARKER)) {
            attachedChatWindows.add(fromClient);
        } else if(matcher.find()) {
            pattern = Pattern.compile("[ ]");
            String[] registrationArray = pattern.split(clientMessage);
            String nickname = registrationArray[1];
            userNicknames.put(fromClient, nickname);
        } else {
            if (bytesInMessage != -1) {
                printToChat(userNicknames.get(fromClient) + ": " + clientMessage);
            } else {
                deleteUser(key);
            }
        }
    }

    // a method to delete user from chat
    private void deleteUser(SelectionKey key) throws IOException {
        SocketChannel user = (SocketChannel) key.channel();
        user.close();
        key.cancel();
        userNicknames.remove(user);
        printToChat("User " + user.socket().getRemoteSocketAddress() + " left the chat");
    }

    // a method to print any kind of messages in chat
    private void printToChat(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());

        for(SocketChannel chatWindow : attachedChatWindows) {
            try {
                chatWindow.write(buffer);
                buffer.clear();
            } catch (ClosedChannelException e) {
                continue;
            }
        }
    }
}
