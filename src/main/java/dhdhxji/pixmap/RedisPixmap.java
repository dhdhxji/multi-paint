package dhdhxji.pixmap;

import redis.clients.jedis.Jedis;

public class RedisPixmap implements DrawInterface {
    public RedisPixmap(Jedis j, int width, int height) throws Exception {
        _jedis = j;

        if(!_jedis.exists(WIDTH_KEY)) {
            _jedis.set(WIDTH_KEY, String.valueOf(width));
        } else if(width != Integer.parseInt(_jedis.get(WIDTH_KEY))) {
            throw new Exception("Canvas width " + width + " is not respect database width " + _jedis.get(WIDTH_KEY));
        }

        if(!_jedis.exists(HEIGHT_KEY)) {
            _jedis.set(HEIGHT_KEY, String.valueOf(height));
        } else if(height != Integer.parseInt(_jedis.get(HEIGHT_KEY))) {
            throw new Exception("Canvas height " + height + " is not respect database height " + _jedis.get(HEIGHT_KEY));
        }

        _w = width;
        _h = height;
    }
    
    @Override
    public int getHeigth() {
        return _h;
    }

    @Override
    public int getWidth() {
        return _w;
    }

    @Override
    public int getPix(int x, int y) {
        return Integer.parseInt(_jedis.get(x + ";" + y));
    }

    @Override
    public void setPix(int x, int y, int color) {
        _jedis.set(x + ";" + y, String.valueOf(color));
    }

    static String WIDTH_KEY = "width";
    static String HEIGHT_KEY = "height";
    private Jedis _jedis;
    private int _w;
    private int _h;
}
