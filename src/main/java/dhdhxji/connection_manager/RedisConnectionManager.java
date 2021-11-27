package dhdhxji.connection_manager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisConnectionManager extends ConnectionManager {
    public RedisConnectionManager(Jedis j, final ProcessCommandListener l, int port) {
        super(l, port);

        JedisPubSub pubsub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                // Process command for redis (it is client-independednt)
                l.process_command(RedisConnectionManager.this, null, message);
            }
        };

        j.subscribe(pubsub, COMMAND_CHANNEL);
    }

    private static String COMMAND_CHANNEL = "command";
}
