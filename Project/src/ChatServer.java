/**
 *
 *  @author Małycha Igor S28192
 *
 */


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private String host;
    private int port;

    private ServerSocketChannel ssc;
    private Selector selector;

    private ConcurrentHashMap<String, SocketChannel> usersOnServer = new ConcurrentHashMap<>();
    private StringBuilder serverLog = new StringBuilder();
    private volatile boolean isRunning;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.isRunning = false;
    }

    public void startServer(){
        try{
            System.out.println("Server started\n");
            isRunning = true;
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(host, port));
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        }catch (Exception e){
            e.printStackTrace();
        }

        new Thread(this::serviceConnections).start();
    }

    public synchronized void serviceConnections(){
        while(isRunning){
            try {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if(key.isAcceptable()){
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                    }else if(key.isReadable()){
                        SocketChannel sc = (SocketChannel) key.channel();
                        serviceRequest(sc);
                    }
                }
            }catch (Exception e){
                e.fillInStackTrace();
            }
        }
    }

    public synchronized void serviceRequest(SocketChannel sc){
        try{
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int len = sc.read(byteBuffer);
            if (len == -1){
                sc.close();
                return;
            }
            String[] msg = new String(byteBuffer.array(), 0, len, StandardCharsets.UTF_8).split("\t");
            byteBuffer.clear();

            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            String formattedTime = currentTime.format(formatter);

            if(msg[0].equals("logged in")){
                usersOnServer.put(msg[1], sc);
                String toSendMsg = msg[1] + " " + msg[0] + "\n";
                serverLog.append(formattedTime).append(" ").append(toSendMsg);
                broadcast(toSendMsg);
            }else if(msg[0].equals("logged out")){
                usersOnServer.remove(msg[1]);
                usersOnServer.put(msg[1], sc);
                String toSendMsg = msg[1] + " " + msg[0] + "\n";
                serverLog.append(formattedTime).append(" ").append(toSendMsg);
                broadcast(toSendMsg);
            }else{
                String toSendMsg = msg[1] + ": " + msg[0] + "\n";
                serverLog.append(formattedTime).append(" ").append(toSendMsg);
                broadcast(toSendMsg);
            }

        }catch (Exception e){
            e.fillInStackTrace();
        }
    }
    public synchronized void broadcast(String msg){
        for(Map.Entry<String, SocketChannel> user : usersOnServer.entrySet()){
            try {
                ByteBuffer byteBuffer = Charset.defaultCharset().encode(CharBuffer.wrap(msg));
                while (byteBuffer.hasRemaining()){
                    user.getValue().write(byteBuffer);
                }
            }catch (Exception e){
                e.fillInStackTrace();
            }
        }
    }
    public void stopServer() throws IOException {
        System.out.println("Server stopped");
        this.isRunning = false;
        selector.close();
        ssc.close();
    }

    public String getServerLog(){
        return serverLog.toString();
    }

}
