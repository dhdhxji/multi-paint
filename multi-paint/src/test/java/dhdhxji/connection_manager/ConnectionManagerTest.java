package dhdhxji.connection_manager;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dhdhxji.connection_manager.IdMap.IdItemHandle;

public class ConnectionManagerTest {
    private static final int PORT = 1234;
    private static ConnectionManager _server = new ConnectionManager(
        new ProcessCommandListener(){
        public void process_command(ConnectionManager server,
            IdItemHandle user, String cmd) {   
                Message resp = new Message();
                if(cmd.equals("echo")) {
                    resp.message = "echo resp";
                    server.send(resp, user);
                } else if(cmd.equals("broadcast")) {
                    resp.message = "broadcast resp";
                    server.broadcast(resp);
                } else {
                    resp.message = "error";
                    server.send(resp, user);
                }
            }
        }, 
        PORT);


    @BeforeClass
    public static void runServerThread() {
        _server.start();
    }

    @AfterClass
    public static void stopServer() {
        _server.stopServer();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    class Connection {
        public Socket clientSock = null;
        public BufferedReader in = null;
        public BufferedWriter out = null;
        
        public Connection(String host) throws UnknownHostException, IOException {
            clientSock = new Socket(host, PORT);
            clientSock.setSoTimeout(200);
            in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
        }
    }

    @Test
    public void testConnectDisconnect() throws UnknownHostException, IOException {
        Connection c = new Connection("localhost");
        c.clientSock.close();

        //if there is no exception, all is okay
    }

    @Test
    public void testPingRequest() throws UnknownHostException, IOException {
        Connection c = new Connection("localhost");

        c.out.write("echo\n");
        c.out.flush();
        String response = c.in.readLine();
        c.clientSock.close();

        assertEquals("echo resp", response);
    }

    @Test
    public void testBroadcast() throws UnknownHostException, IOException {
        Connection c1 = new Connection("localhost");
        Connection c2 = new Connection("localhost");

        c1.out.write("broadcast\n");
        c1.out.flush();

        String c1Resp = c1.in.readLine();
        String c2Resp = c2.in.readLine();

        c1.clientSock.close();
        c2.clientSock.close();

        assertEquals("c1 connection not broadcast response: " + c1Resp, "broadcast resp", c1Resp);
        assertEquals("c2 connection not broadcast response: " + c2Resp, "broadcast resp", c2Resp);
    }
}
