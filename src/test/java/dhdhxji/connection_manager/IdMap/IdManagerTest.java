package dhdhxji.connection_manager.IdMap;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.PriorityBlockingQueue;

import org.junit.Test;

public class IdManagerTest {
    //IdManager tests
    @Test
    public void testIdTake() {
        IdManager testManager = new IdManager();
        int id = testManager.getId();

        assertEquals(0, id);
    }

    @Test
    public void testIdReuse() {
        IdManager testManager = new IdManager();
        for(int i = 0; i < 10; ++i) {
            testManager.getId();
        }

        testManager.freeId(5);

        int testId = testManager.getId();
        assertEquals(5, testId);

        for(int i = 0; i < 10; ++i) {
            testManager.freeId(i);
        }

        assertEquals(0, testManager.getId());
        assertEquals(1, testManager.getId());
    }

    @Test
    public void testConcurrentTake() {
        final int threadsCount = 100;
        final int loopNumber = 100;

        assertEquals(0, loopNumber%2);

        final IdManager testManager = new IdManager();
        final PriorityBlockingQueue<Integer> ids = 
                new PriorityBlockingQueue<Integer>();

        class TestingThread extends Thread {
            public void run() {
                for(int i = 0; i < loopNumber; ++i) {
                    ids.add(testManager.getId());
                }
            }
        }

        TestingThread[] threads = new TestingThread[threadsCount];
        for(int i = 0; i < threadsCount; ++i) {
            threads[i] = new TestingThread();
        }

        for(int i = 0; i < threadsCount; ++i) {
            threads[i].start();
        }

        for(int i = 0; i < threadsCount; ++i) {
            try{
                threads[i].join();
            } catch(InterruptedException e) {}
        }

        final int count = ids.size();
        for(int i = 0; i < count; ++i) {
            assertEquals(i, (int)ids.poll());
        }
    }

    @Test
    public void testConcurrentGive() {
        final IdManager testManager = new IdManager();
        final PriorityBlockingQueue<Integer> ids = 
            new PriorityBlockingQueue<Integer>();

        for(int i = 0; i < 10000; ++i) {
            ids.add(testManager.getId());
        }

        class TestingThread extends Thread {
            public void run() {
                while(true) {
                    Integer id = ids.poll();
                    if(id == null) {
                        break;
                    }

                    testManager.freeId(id);
                }
            }
        }

        final int threadsCount = 50;
        TestingThread[] threads = new TestingThread[threadsCount];
        for(int i = 0; i < threadsCount; ++i) {
            threads[i] = new TestingThread();
        }

        for(int i = 0; i < threadsCount; ++i) {
            threads[i].start();
        }

        for(int i = 0; i < threadsCount; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {}
        }

        assertEquals(0, testManager.getId());
    }
}