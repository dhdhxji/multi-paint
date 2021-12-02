package dhdhxji.pixmap;

import java.util.Vector;

public class DrawPixmap implements DrawInterface {
    public DrawPixmap(int width, int height) {
        _pixmap = new int[width*height];
        _width = width;
        _height = height;

        for(int i = 0; i < width*height; ++i) {
            _pixmap[i] = 0xffffff;
        }
    } 


    public void setPix(int x, int y, int color) throws IndexOutOfBoundsException {
        int index = y*_width + x;
        if(index >= _pixmap.length || index < 0) {
            return;
        }

        synchronized(_mutex)
        {
            _pixmap[index] = color;
        }
    }

    public int getPix(int x, int y) throws IndexOutOfBoundsException {
        int index = y*_width + x;

        synchronized(_mutex) {
            return _pixmap[index];
        }
    }

    @Override
    public void setMultiPix(Pixel[] pix) {
        for(Pixel p: pix) {
            setPix(p.pos.x, p.pos.y, p.color);
        }
    }

    @Override
    public Pixel[] getMultiPix(Vec2[] pos) {
        Pixel[] p = new Pixel[pos.length];

        for(int i = 0; i < pos.length; ++i) {
            p[i].pos = pos[i];
            p[i].color = getPix(pos[i].x, pos[i].y);
        }

        return p;
    }

    @Override
    public Strip[] getNonZeroStrips() {
        Vector<Strip> strips = new Vector<>();

        int startXPos = 0;
        int startYPos = 0;
        Vector<Integer> pix_to_send = new Vector<Integer>();

        for(int y = 0; y < getHeigth(); ++y) {
            for(int x = 0; x < getWidth(); ++x) {
                int color = getPix(x, y); 
                
                if(color != 0xffffff) {
                    if(pix_to_send.size() == 0) {
                        startXPos = x;
                        startYPos = y;
                    }
                    pix_to_send.add(color);
                } else {
                    if(pix_to_send.size() != 0) {
                        int[] intArr = new int[pix_to_send.size()];
                        for(int i = 0; i < pix_to_send.size(); ++i) {
                            intArr[i] = pix_to_send.get(i).intValue();
                        }
                        pix_to_send.clear();
    
                        Strip s = new Strip();
                        s.x_start = startXPos;
                        s.y_start = startYPos;
                        s.colors = intArr;
                        strips.add(s);
                    } 
                }
            }
        }

        if(pix_to_send.size() != 0) {
            int[] intArr = new int[pix_to_send.size()];
            for(int i = 0; i < pix_to_send.size(); ++i) {
                intArr[i] = pix_to_send.get(i).intValue();
            }

            Strip s = new Strip();
            s.x_start = startXPos;
            s.y_start = startYPos;
            s.colors = intArr;
            strips.add(s);
        }

        return strips.toArray(new Strip[strips.size()]);
    }

    public int getWidth() {
        return _width;
    }

    public int getHeigth() {
        return _height;
    }

    Object _mutex = new Object();

    int _width = 0;
    int _height = 0;
    int[] _pixmap = null;
}
