import java.net.ServerSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface processCommandListener {
    public void process_command(String command);
}

public class ConnectionManager {
    private processCommandListener _commandHandler = null;
    private Queue<String> _sendingQueue = null;

    public ConnectionManager(processCommandListener l) {
        _commandHandler = l;
        _sendingQueue = new ConcurrentLinkedQueue<String>();
    }

    public void send_to_all(String command) {
        
    }

    public void run(int port) {

    }
}