package dhdhxji.connection_manager.IdMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IdMapTest {
    private IdItemHandle[] generate_sequence(int count, 
                                             IdMap<Integer> map) {
        IdItemHandle[] handles = new IdItemHandle[count];
        for(int i = 0; i < 10; ++i) {
            handles[i] = map.add(i);
        }

        return handles;
    } 

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testStore() {
        IdMap<Integer> testmap = new IdMap<Integer>();
        IdItemHandle[] handles = generate_sequence(10, testmap);

        for(int i = 0; i < handles.length; ++i) {
            assertEquals(i, (int)testmap.get(handles[i]));
        }
    }

    @Test
    public void testRemove() {
        IdMap<Integer> testMap = new IdMap<Integer>();
        IdItemHandle h = testMap.add(5);
        testMap.remove(h);

        exception.expect(NoSuchElementException.class);
        //should throw exception
        testMap.get(h);
    }

    @Test
    public void testInvalidHandle() {
        IdMap<Integer> testMap = new IdMap<Integer>();
        testMap.add(5);

        exception.expect(NoSuchElementException.class);
        testMap.get(new IdItemHandle());
    }

    @Test
    public void testExpiredHandle() {
        IdMap<Integer> testMap = new IdMap<Integer>();
        IdItemHandle h = testMap.add(5);

        testMap.remove(h);

        exception.expect(NoSuchElementException.class);
        testMap.get(h);
    }

    @Test
    public void testHandleReuse() {
        IdMap<Integer> testmap = new IdMap<Integer>();
        IdItemHandle[] handles = generate_sequence(10, testmap);

        testmap.remove(handles[2]);
        IdItemHandle h = testmap.add(2);

        assertEquals(h, handles[2]);
    }

    @Test
    public void testForeignMapHandle() {
        IdMap<Integer> testmap1 = new IdMap<Integer>();
        IdItemHandle h1 = testmap1.add(5);

        IdMap<Integer> testmap2 = new IdMap<Integer>();
        IdItemHandle h2 = testmap2.add(5);

        assertNotEquals(h1, h2);
    }

    @Test
    public void testConcurrentStoring() {
        final IdMap<Integer> testMap = new IdMap<Integer>();
        final Vector<IdItemHandle> handles = new Vector<IdItemHandle>();

        final int threadCount = 30;
        final int objectsCount = 100;

        assertEquals(0, objectsCount%2);

        class TestingThread extends Thread {
            public void run() {
                for(int i = 0; i < objectsCount; ++i) {
                    handles.add(testMap.add(i));
                }
            }
        }

        TestingThread[] threads = new TestingThread[threadCount];
        for(int i = 0; i < threads.length; ++i) {
            threads[i] = new TestingThread();
            threads[i].start();
        }

        for(int i = 0; i < threads.length; ++i) {
            try{
                threads[i].join();
            } catch(InterruptedException e) {}
        }

        int total = 0;
        for (IdItemHandle handle : handles) {
            total += testMap.get(handle);
        }

        assertEquals(((objectsCount-1)*(objectsCount/2))*threadCount, total);
    }

    @Test
    public void testConcurrentRemove() {
        final IdMap<Integer> testMap = new IdMap<Integer>();
        final ConcurrentLinkedQueue<IdItemHandle> handles = 
                new ConcurrentLinkedQueue<IdItemHandle>();
        
        final int loopNumber = 3000;
        final int threadCount = 50;

        for(int i = 0; i < loopNumber; ++i) {
            handles.add(testMap.add(i));
        }

        final ConcurrentLinkedQueue<IdItemHandle> testHandles = 
        new ConcurrentLinkedQueue<IdItemHandle>(handles);

        class TestingThread extends Thread {
            public void run() {
                while(true) {
                    IdItemHandle h = handles.poll();
                    if(h == null) {
                        return;
                    }
                    testMap.remove(h);
                }
            }
        }

        TestingThread[] threads = new TestingThread[threadCount];
        for(int i = 0; i < threadCount; ++i) {
            threads[i] = new TestingThread();
        }

        for(int i = 0; i < threadCount; ++i) {
            threads[i].start();
        }

        for(int i = 0; i < threadCount; ++i) {
            try {
                threads[i].join();
            } catch(InterruptedException e) {}
        }

        for (IdItemHandle h : testHandles) {
            exception.expect(NoSuchElementException.class);
            testMap.get(h);
        }
    }
}
