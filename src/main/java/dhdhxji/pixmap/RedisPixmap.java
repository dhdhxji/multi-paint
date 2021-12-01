package dhdhxji.pixmap;

import java.util.List;
import java.util.Vector;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;


public class RedisPixmap extends DrawPixmap {
    public RedisPixmap(String redis_addr, int width, int height) throws Exception {
        super(width, height);
        _jedis = new Jedis(redis_addr);

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

        // Get pixmap from redis
        syncWithRedis();

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

    /* @Override
    public int getPix(int x, int y) {
        String cell;
        synchronized(_jedis) {
            cell = _jedis.get(x + ";" + y);
        }
        if(cell == null || cell.equals("")) {
            return 0xffffff;
        } else {
            return Integer.parseInt(cell);
        }
    } */

    @Override
    public void setPix(int x, int y, int color) {
        super.setPix(x, y, color);
        
        String key = x + ";" + y;
        synchronized(_jedis) {
            if(color == 0xffffff) {
                _jedis.del(key);
            } else { 
                _jedis.set(key, String.valueOf(color));
            }
        }    
    }

    @Override
    public void setMultiPix(Pixel[] pix) {
       // String[] keysvalues = new String[pix.length*2];
        Vector<String> keysvalues = new Vector<>();
        
        for(int i = 0; i < pix.length; ++i) {
            int x = pix[i].pos.x;
            int y = pix[i].pos.y;
            int color = pix[i].color;

            try {
                if(getPix(x, y) == color) {
                    continue;
                }
            } catch(ArrayIndexOutOfBoundsException e) {/*Do nothing*/}
            keysvalues.add(x + ";" + y);
            keysvalues.add(String.valueOf(color));
        }

        super.setMultiPix(pix);

        if(keysvalues.size() != 0) {
            synchronized(_jedis) {
                _jedis.mset(keysvalues.toArray(new String[keysvalues.size()]));
            }
        }
        
    }

/*     @Override
    public Strip[] getNonZeroStrips() {
        String iterator = "0";
        ScanParams p = new ScanParams();
        p.match("*;*");

        // Get all stored keys
        Vector<String> keys = new Vector<>();
        ScanResult<String> r = null;
        while( !(r = _jedis.scan(iterator, p)).getCursor().equals("0") ) {
            keys.addAll(r.getResult());
        }

        // Transform keys to coordiantes, sort it
        for(key)
    } */

    private void syncWithRedis() {
        String iterator = "0";
        ScanParams p = new ScanParams();
        p.match("*;*");

        // Get all stored keys
        Vector<String> keys = new Vector<>();
        ScanResult<String> r = null;
        
        do {
            synchronized(_jedis) {
                r = _jedis.scan(iterator, p);
            }
            iterator = r.getCursor();
            keys.addAll(r.getResult());
        }
        while( !iterator.equals("0") );

        if(keys.size() == 0) {
            return;
        }

        List<String> res = null;
        synchronized(_jedis) {
            res = _jedis.mget(keys.toArray(new String[keys.size()]));
        }

        for(int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            int x = Integer.parseInt( key.split(";")[0]);
            int y = Integer.parseInt( key.split(";")[1]);

            int color = Integer.parseInt(res.get(i));

            super.setPix(x, y, color);
        }        
    }

    static String WIDTH_KEY = "width";
    static String HEIGHT_KEY = "height";
    private Jedis _jedis;
    private int _w;
    private int _h;
}
