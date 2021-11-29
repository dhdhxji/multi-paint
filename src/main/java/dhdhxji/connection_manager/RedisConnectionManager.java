package dhdhxji.connection_manager;

import java.io.InvalidObjectException;

import dhdhxji.command.marshaller.Command;
import dhdhxji.command.marshaller.Marshaller;
import dhdhxji.command.marshaller.commandDataImpl.CircleCmd;
import dhdhxji.command.marshaller.commandDataImpl.LoginCmd;
import dhdhxji.command.marshaller.commandDataImpl.SetCmd;
import dhdhxji.command.marshaller.commandDataImpl.SizeCmd;
import dhdhxji.command.marshaller.commandDataImpl.StripCmd;
import dhdhxji.connection_manager.IdMap.IdItemHandle;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisConnectionManager extends ConnectionManager {
    public RedisConnectionManager(String redis_addr, final ProcessCommandListener l, int port) {
        super(l, port);

        final JedisPubSub pubsub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                // Process commands from redis pub/sub channel
                // Commands travels to here from other servers and by client requests (set/circle/stri)
                l.process_command(RedisConnectionManager.this, null, message);
            }
        };
        _listener = new Jedis(redis_addr);
        _publisher = new Jedis(redis_addr);
        _pubSub = pubsub;
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                _listener.subscribe(pubsub, COMMAND_CHANNEL);
                System.out.println("end of listening");
            }
        }).start();
    }

    @Override
    public void stopServer() {
        // Stop the redis polling thread
        _pubSub.unsubscribe();
        super.stopServer();
    }

    @Override
    protected void processClientCommand(IdItemHandle client, String command) {
        // TODO: do this in proper way (ConnectionManager should know nothing about serialization).
        Marshaller temp_deserializer = new Marshaller()
            .registerCommand("login", LoginCmd.class)
            .registerCommand("set", SetCmd.class)
            .registerCommand("size", SizeCmd.class)
            .registerCommand("strip", StripCmd.class)
            .registerCommand("circle", CircleCmd.class);
            
        try {
            Command raw_cmd = temp_deserializer.deserialize(command);
            if(raw_cmd.commandName.equals("set") || raw_cmd.commandName.equals("circle")) {
                // If it is circle or set, broadcast it over redis
                synchronized(_publisher) {
                    _publisher.publish(COMMAND_CHANNEL, command);
                }
            } else {
                super.processClientCommand(client, command);
            }
        } catch(InvalidObjectException e) {
            System.err.println("Invalid command: " + command + "; Exception: " + e);
        }
    }

    private Jedis _listener;
    private Jedis _publisher;
    private JedisPubSub _pubSub;
    private static String COMMAND_CHANNEL = "command";
}
