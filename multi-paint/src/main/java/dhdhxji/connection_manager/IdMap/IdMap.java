package dhdhxji.connection_manager.IdMap;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;



public class IdMap<T> {
    
    public IdItemHandle add(T item) {
        IdItemHandle h = new IdItemHandle();
        h.id = _idManager.getId();
        h.containerHash = System.identityHashCode(this);
        h.itemHash = System.identityHashCode(item);
        
        placeItem(h, item);
        return h;
    }

    public T remove(IdItemHandle h) throws NoSuchElementException{
        T data = get(h);

        freeItem(h);
        _idManager.freeId(h.id);

        return data;
    }

    public T get(IdItemHandle h) throws NoSuchElementException {
        try {
            if( h.containerHash != System.identityHashCode(this) || 
                _data.get(h.id) == null ||
                h.itemHash != System.identityHashCode(_data.get(h.id).data)) {
                throw new NoSuchElementException();
            }
            
            return _data.get(h.id).data;
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }


    
    private void placeItem(IdItemHandle h, T data) {
        Item<T> item = new Item<T>();
        item.data = data;

        final int requestedSize = h.id+1;
        
        while(_data.size() < requestedSize) {
            _data.add(null);
        }

        _data.set(h.id, item);
    }

    private void freeItem(IdItemHandle h) throws NoSuchElementException {
        synchronized(_mutex) {
            _data.get(h.id).data = null;
        }
    }

    private Object _mutex = new Object();
    private Vector<Item<T>> _data = new Vector<Item<T>>();
    private IdManager _idManager = new IdManager();
}



class Item<T> {
    public T data;
}



class IdManager {
    public int getId() {
        synchronized(_mutex) {
            if(!_freeIds.isEmpty()) {
                return _freeIds.poll();
            } else {
                return _lastFreeId++;
            }
        }
    }

    public void freeId(int id) {
        synchronized(_mutex) {
            if(id >= _lastFreeId) {
                return;
            }

            if(_freeIds.contains(id)) {
                return;
            }

            if(id == _lastFreeId -1) {
                _lastFreeId--;
            } else {
                _freeIds.add(id);
            }

            //clean id`s in free id queue
            if(!_freeIds.isEmpty()) {                
                while(  _lastFreeId != 0 &&
                        _freeIds.peek() == _lastFreeId-1) {
                    _freeIds.poll();
                    _lastFreeId--;
                }
            }
        }
    }

    private Object _mutex = new Object();
    private int _lastFreeId = 0;
    private PriorityBlockingQueue<Integer> _freeIds = 
        new PriorityBlockingQueue<Integer>(1, Collections.reverseOrder());
}