package dhdhxji.connection_manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import dhdhxji.connection_manager.IdMap.IdItemHandle;
import dhdhxji.connection_manager.IdMap.IdMap;


public class ConnectionManager extends Thread {
    public ConnectionManager(ProcessCommandListener l, int port) {
        _commandHandler = l;
        _port = port;
    }

    public void send(Message cmd, IdItemHandle receiver) {
        SendingItem i = new SendingItem();
        i.cmd = cmd;
        i.broadcast = false;
        i.receiver = receiver;

        _sendingQueue.add(i);
    }

    public void broadcast(Message cmd) {
        for (IdItemHandle client : _client_handles) {
            send(cmd, client);
        }
    }

    @Override
    public void run() {
        ServerSocket listen = null;

        try {
            listen = new ServerSocket(_port);

            //Start sending thread
            Sender sendThread = new Sender();
            sendThread.start();

            //allow new connections
            while(true) {
                Socket sock = listen.accept();
                System.out.print("New client connection");
                
                //store client
                Client client = new Client();
                client.read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                client.write = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                
                
                IdItemHandle client_handle = _clients.add(client);
                _client_handles.add(client_handle);

                //run new listening thread
                new ClientListener(client_handle).start();
            }
        }
        catch(IOException e) {
            System.err.print("Can not start connection maanger: " + e);
        }
        finally {
            try {
                if(listen != null)
                    listen.close();
            }
            catch(IOException e) {
                System.err.print("Can`t close listening socket: " + e);
            }
        }
    }



    private ProcessCommandListener _commandHandler = null;
    private int _port = 0;
    private BlockingQueue<SendingItem> _sendingQueue = new LinkedBlockingQueue<SendingItem>();

    private IdMap<Client> _clients = new IdMap<Client>();
    private Queue<IdItemHandle> _client_handles = new ConcurrentLinkedQueue<IdItemHandle>();



    class Sender extends Thread {
        private void send(Message msg, IdItemHandle h) {
            try {
                _clients.get(h).write.write(msg.message + "\n");
            } catch(IOException e) {
                //disconnected
                _clients.remove(h);
            }    
        }

        public void run() {
            while(true) {
                try {
                    SendingItem item = _sendingQueue.take(); 
                    if(item.broadcast) {
                        for (IdItemHandle h : _client_handles) {
                            send(item.cmd, h);
                        }
                    } else {
                        send(item.cmd, item.receiver);
                    }
                } catch(InterruptedException e) {
                    //do nothing
                }          
            }
        }
    }

    class ClientListener extends Thread {
        public ClientListener(IdItemHandle client_handle) {
            _client_handle = client_handle;
        }

        public void run() {
            System.out.print("New client thread");

            BufferedReader reader = _clients.get(_client_handle).read;

            while(true) {
                try {
                    String command = reader.readLine();

                    _commandHandler.process_command(
                        ConnectionManager.this,
                        _client_handle, 
                        command);
                }
                catch (IOException e) {
                    System.err.print("Client disconnected");
                    _clients.remove(_client_handle);
                    return;
                }
            }
        }

        private IdItemHandle _client_handle = null;
    }
}

class Client {
    BufferedReader read;
    BufferedWriter write;
}

class SendingItem{
    Message cmd;
    IdItemHandle receiver;
    boolean broadcast;
}