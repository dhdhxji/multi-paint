package dhdhxji.pixmap;

public interface DrawInterface {
    public void setPix(int x, int y, int color);
    public int getPix(int x, int y);

    public void setMultiPix(Pixel[] pix);
    public Pixel[] getMultiPix(Vec2[] pos);

    public Strip[] getNonZeroStrips();

    public int getWidth();
    public int getHeigth();
}
