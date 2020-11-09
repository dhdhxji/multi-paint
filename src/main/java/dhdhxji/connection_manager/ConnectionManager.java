package dhdhxji.connection_manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
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
        try {
            _listenSocket = new ServerSocket(_port);
            _listenSocket.setSoTimeout(1000);
        }
        catch(IOException e) {
            System.err.print("Can not start connection maanger: " + e);
            return;
        }

        //Start sending thread
        Sender sendThread = new Sender();
        sendThread.setName("sender");
        sendThread.start();

        //allow new connections
        while(true) {
            try {
                Socket sock = _listenSocket.accept();
                System.out.print("New client connection");

                //store client
                Client client = new Client();
                client.read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                client.write = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                client.sock = sock;

                IdItemHandle client_handle = _clients.add(client);
                _client_handles.add(client_handle);
                
                //run new listening thread
                ClientListener l = new ClientListener(client_handle);
                l.setName("listener");
                l.start();

            } catch(SocketTimeoutException e) {
                if(isInterrupted())
                    break;
                else
                    continue;
            } catch(IOException e) {
                System.err.print("Can not accept new connection: " + e);
            }
        }

        //disable listening
        try {
            _listenSocket.close();
        } catch(IOException e) {}

        //stop the sending thread
        sendThread.interrupt();
        try {
            sendThread.join();
        } catch(InterruptedException e) {}

        //stop all client listener threads
        for (IdItemHandle h : _client_handles) {
            freeClientResources(h);
        }
    }

    public void stopServer() {
        this.interrupt();
        try {
            this.join();     
        } catch (InterruptedException e) {}
    }

    /*
    *       This method should close socket associates with client, so
    *   IOException will be throwed in client listenning thread
    *   and it will be closed.
    */
    private void freeClientResources(IdItemHandle h) {
        try {
            Client client = _clients.remove(h);
            synchronized(_mutex) {
                try {client.sock.shutdownOutput();  } catch(IOException e) {}
                try {client.sock.shutdownInput();   } catch(IOException e) {}
                try {client.write.close();          } catch(IOException e) {}
                try {client.read.close();           } catch(IOException e) {}
                try {client.sock.close();           } catch(IOException e) {}
            }
        } catch(NoSuchElementException e) {
            //user does not exist
        }
    }

    private Object _mutex = new Object();

    private ProcessCommandListener _commandHandler = null;
    private BlockingQueue<SendingItem> _sendingQueue = new LinkedBlockingQueue<SendingItem>();
    
    private int _port = 0;
    private ServerSocket _listenSocket = null;

    private IdMap<Client> _clients = new IdMap<Client>();
    private Queue<IdItemHandle> _client_handles = new ConcurrentLinkedQueue<IdItemHandle>();



    class Sender extends Thread {
        private void send(Message msg, IdItemHandle h) {
            try {
                Client client = _clients.get(h);
                client.write.write(msg.message + "\n");
                client.write.flush();
            } catch(IOException e) {
                //disconnected
                freeClientResources(h);
            } catch(NoSuchElementException e) {
                //client was disconnected
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

                    if(isInterrupted()) {
                        throw new InterruptedException();
                    }
                } catch(InterruptedException e) {
                    //thread interrut from outside, exit
                    return;
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
                    if(command == null) {
                        //end of the stream, connection closed
                        throw new IOException();
                    }

                    _commandHandler.process_command(
                        ConnectionManager.this,
                        _client_handle, 
                        command);
                }
                catch (IOException e) {
                    System.err.print("Client disconnected");
                    freeClientResources(_client_handle);
                    return;
                }
            }
        }

        private IdItemHandle _client_handle = null;
    }
}

class Client {
    Socket sock;
    BufferedReader read;
    BufferedWriter write;
}

class SendingItem{
    Message cmd;
    IdItemHandle receiver;
    boolean broadcast;
}