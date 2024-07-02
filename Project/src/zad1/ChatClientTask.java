/**
 *
 *  @author Małycha Igor S28192
 *
 */

package zad1;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ChatClientTask implements Runnable{
    ChatClient c;
    List<String> msgs;
    int wait;

    FutureTask<String> future;

    public ChatClientTask(ChatClient c, List<String> msgs, int wait) {
        this.c = c;
        this.msgs = msgs;
        this.wait = wait;
        this.future = new FutureTask<>(() ->{
            c.login();
            if (wait != 0) Thread.sleep(wait);
            for (String req : msgs) {
                c.send(req);
                if (wait != 0) Thread.sleep(wait);
            }
            c.logout();
            if (wait != 0) Thread.sleep(wait);
            return c.getChatView();
        });
    }

    @Override
    public void run(){
        future.run();
    }
    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait){
        return new ChatClientTask(c, msgs, wait);
    }
    public String get() throws InterruptedException, ExecutionException {
        return this.future.get();
    }
    public ChatClient getClient() {
        return c;
    }
}
