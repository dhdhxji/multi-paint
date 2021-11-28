package dhdhxji.pixmap;

public interface DrawInterface {
    public void setPix(int x, int y, int color);
    public int getPix(int x, int y);

    public Strip[] getNonZeroStrips();

    public int getWidth();
    public int getHeigth();
}
