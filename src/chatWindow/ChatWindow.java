package chatWindow;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class ChatWindow {

    InetSocketAddress serverAddress;
    SocketChannel fromServer;
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    String message;

    public ChatWindow(String host, int port) {
        serverAddress = new InetSocketAddress(host, port);
    }

    public void start() {
        try {
            Selector selector = Selector.open();
            fromServer = SocketChannel.open(serverAddress);
            fromServer.configureBlocking(false);
            fromServer.register(selector, SelectionKey.OP_WRITE);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey key : selectedKeys) {
                    if(key.isWritable()) {
                        connectToServer(key);
                    }

                    // except attaching to server, chat window channel is always readable
                    if (key.isReadable()) {
                        printToWindow(key);
                    }
                }
                selectedKeys.clear();
            }
        } catch (SocketException e) {
            System.out.println("Chat was closed by administrator.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectToServer(SelectionKey key) throws IOException {
        SocketChannel toServer = (SocketChannel) key.channel();
        byte[] connectingMessage = "CHATWINDOW".getBytes();
        buffer = buffer.put(connectingMessage);
        buffer.limit(connectingMessage.length);
        toServer.write(buffer);
        buffer.clear();
        key.interestOps(SelectionKey.OP_READ);
    }

    // a method to print all chat messages to chat window
    private void printToWindow(SelectionKey key) throws IOException {
        fromServer = (SocketChannel) key.channel();

        fromServer.read(buffer);
        buffer.flip();

        message = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();

        System.out.println(message);
    }
}
