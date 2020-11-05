package dhdhxji.connection_manager.IdMap;

public class IdItemHandle extends Object{
    protected IdItemHandle() {}
    protected int id;
    protected int containerHash;
    protected int itemHash;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (getClass() != o.getClass())
            return false;

        IdItemHandle h = (IdItemHandle) o;
        return id == h.id && 
               containerHash == h.containerHash && 
               itemHash == h.itemHash;
    }
}
