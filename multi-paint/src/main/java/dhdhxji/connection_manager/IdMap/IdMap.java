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
                _data.get(h.id).in_rent == false ||
                h.itemHash !=System.identityHashCode(_data.get(h.id).data)) {
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
        item.in_rent = true;
        
        //if _data[id] is next element
        if(h.id == _data.size()) {
            _data.add(item);
        } else if(h.id < _data.size()) {
            _data.set(h.id, item);
        }
    }

    private void freeItem(IdItemHandle h) throws NoSuchElementException {
        _data.get(h.id).in_rent = false;
        _data.get(h.id).data = null;
    }

    
    private Vector<Item<T>> _data = new Vector<Item<T>>();
    private IdManager _idManager = new IdManager();
}



class Item<T> {
    public boolean in_rent;
    public T data;
}



class IdManager {
    public int getId() {
        if(!_freeIds.isEmpty()) {
            return _freeIds.poll();
        } else {
            synchronized(_mutex) {
                return _lastFreeId++;
            }
        }
    }

    public void freeId(int id) {
        if(id == _lastFreeId -1) {
            synchronized(_mutex) {
                _lastFreeId--;
            }

            //clean id`s in free id queue
            if(!_freeIds.isEmpty()) {                
                while(_freeIds.peek() == _lastFreeId-1) {
                    _freeIds.poll();
                }
            }
        } else {
            _freeIds.add(id);
        }
    }

    private Object _mutex = new Object();
    private int _lastFreeId = 0;
    private PriorityBlockingQueue<Integer> _freeIds = 
        new PriorityBlockingQueue<Integer>(1, Collections.reverseOrder());
}