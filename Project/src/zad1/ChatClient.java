/**
 *
 *  @author Małycha Igor S28192
 *
 */

package zad1;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ChatClient {

    String host;
    int port;
    String id;

    SocketChannel socketChannel;
    StringBuilder chatView = new StringBuilder();

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
        chatView.append("=== "+ this.id + "chat view\n");
    }

    public void login(){
        try{
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);
            String msg = "logged in" +"\t"+ this.id;
            ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
            while(byteBuffer.hasRemaining()){
                socketChannel.write(byteBuffer);
            }
            setListeningThread();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setListeningThread(){
        new Thread(() -> {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            try{
                while(socketChannel.isOpen()){
                    byteBuffer.clear();
                    int len = socketChannel.read(byteBuffer);
                    if(len == -1){
                        break;
                    }
                    byteBuffer.flip();

                    //String msg = new String(byteBuffer.array(), 0, len, StandardCharsets.UTF_8);
                    String msg = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                    chatView.append(msg);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public void logout(){
        try {
            String msg = "logged out" + "\t" + this.id;
            ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
            while(byteBuffer.hasRemaining()){
                socketChannel.write(byteBuffer);
            }
            //Thread.sleep(1000);
            socketChannel.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void send(String req){
        try{
            String toSend = req + "\t" +id;
            ByteBuffer byteBuffer = ByteBuffer.wrap(toSend.getBytes());
            while(byteBuffer.hasRemaining()){
                socketChannel.write(byteBuffer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getChatView(){
        return chatView.toString();
    }
}
