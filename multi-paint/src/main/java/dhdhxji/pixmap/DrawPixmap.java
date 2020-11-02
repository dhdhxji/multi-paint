package dhdhxji.pixmap;

public class DrawPixmap implements DrawInterface {
    public DrawPixmap(int width, int height) {
        _pixmap = new int[width*height];
        _width = width;
        _height = height;

        for(int i = 0; i < width*height; ++i) {
            _pixmap[i] = 0;
        }
    } 


    public void setPix(int x, int y, int color) throws IndexOutOfBoundsException {
        int index = y*_width + x;

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

    Object _mutex = new Object();

    int _width = 0;
    int _height = 0;
    int[] _pixmap = null;
}
