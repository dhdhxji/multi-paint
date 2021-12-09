package dhdhxji.pixmap;

public class Pixel {
    public Vec2 pos;
    public int color;

    public Pixel(Vec2 p, int c) {
        pos = p;
        color = c;
    }

    public Pixel(int x, int y, int c) {
        pos = new Vec2(x, y);
        color = c;
    }
};
