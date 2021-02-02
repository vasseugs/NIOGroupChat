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

    InetSocketAddress windowAddress;
    ServerSocketChannel windowChannel;
    SocketChannel fromServer;
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    String message;

    public ChatWindow(String host, int port) {
        windowAddress = new InetSocketAddress(host, port);
    }

    public void start() {
        try {
            Selector selector = Selector.open();
            windowChannel = ServerSocketChannel.open();
            windowChannel.bind(windowAddress);
            windowChannel.configureBlocking(false);
            windowChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey key : selectedKeys) {
                    if (key.isAcceptable()) {
                        bindToServer(selector, key);
                    }

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

    // a method to bind chat window to server
    private void bindToServer(Selector selector, SelectionKey key) throws IOException {
        windowChannel = (ServerSocketChannel) key.channel();
        fromServer = windowChannel.accept();
        fromServer.configureBlocking(false);
        fromServer.register(selector, SelectionKey.OP_READ, buffer);
    }

    // a method to print all chat messages to chat window
    private void printToWindow(SelectionKey key) throws IOException {
        fromServer = (SocketChannel) key.channel();
        buffer = (ByteBuffer) key.attachment();

        fromServer.read(buffer);
        buffer.flip();

        message = new String(buffer.array(), buffer.position(), buffer.limit());
        buffer.clear();

        System.out.println(message);
    }
}
