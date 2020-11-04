package dhdhxji.pixmap;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DrawPixmapTest {
    @Test
    public void testSetPixel() {
        DrawPixmap p = new DrawPixmap(200, 200);
        p.setPix(0, 0, 50);

        assertEquals(50, p.getPix(0, 0));
    }

    @Test
    public void testFillArea() {
        DrawPixmap p = new DrawPixmap(200, 200);
        for(int x = 0; x < 200; ++x) {
            for(int y = 0; y < 200; ++y) {
                p.setPix(x, y, y*200+x);
            }
        }

        for(int x = 0; x < 200; ++x) {
            for(int y = 0; y < 200; ++y) {
                assertEquals(y*200+x, p.getPix(x, y));
            }
        }
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testOutOfBounds() {
        DrawPixmap p = new DrawPixmap(200, 200);

        exception.expect(IndexOutOfBoundsException.class);
        p.setPix(201, 201, 0);
    }

    @Test
    public void testSize() {
        DrawPixmap p = new DrawPixmap(203, 204);

        assertEquals(203, p.getWidth());
        assertEquals(204, p.getHeigth());
    }
}
