package chatWindow;

import programConstants.ProgramConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class ChatWindow {

    InetSocketAddress serverAddress;
    SocketChannel serverChannel;
    ByteBuffer buffer = ByteBuffer.allocate(512);  // the only buffer because we have only one channel
    String message;  // the only message field because we have only one channel

    public ChatWindow(String serverHost, int serverPort) throws UnknownHostException {
        serverAddress = new InetSocketAddress(serverHost, serverPort);
    }

    // a method that starts chat window and manages all events
    public void start() {
        try {
            Selector selector = Selector.open();
            serverChannel = SocketChannel.open(serverAddress);
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_READ);

            attachToServer();

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey key : selectedKeys) {

                    // chat window channel is always readable
                    if (key.isReadable()) {
                        printToWindow(key);
                    }
                }
                selectedKeys.clear();
            }
        } catch (SocketException e) {
            System.out.println(ProgramConstants.SERVER_DISCONNECTED);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // this method attaches chat window to server by sending special marker ia a message
    private void attachToServer() throws IOException {
        byte[] connectingMessage =
                ProgramConstants.CHAT_WINDOW_MARKER.getBytes();
        buffer = buffer.put(0, connectingMessage);
        buffer.limit(connectingMessage.length);
        serverChannel.write(buffer);
        buffer.clear();
        System.out.println(ProgramConstants.CONNECTED_TO_SERVER);
    }

    // a method to print all chat messages to chat window
    private void printToWindow(SelectionKey key) throws IOException {
        serverChannel = (SocketChannel) key.channel();

        serverChannel.read(buffer);
        buffer.flip();

        message = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();

        System.out.println(message);
    }
}
