package nars.bag;

import nars.budget.Budget;
import nars.budget.Itemized;

/** transaction interface for lazily constructing bag items, and efficiently updating existing items.
  * this avoids construction when only updating the budget of an item already in the bag  */
public interface BagTransaction<K,V extends Itemized<K>>  {
    //TODO make a version which accepts an array or list of keys to select in batch

    ////TODO called before anything, including name().  return false to cancel the process before anything else happens */
    //default void start() {    }

    /** item's key; if null, the bag will use a peekNext operation to as the next item */
    public K name();

    /** called if putIn a bag and the item specified by the key doesn't exist,
     * so this will create it and the bag will insert the new instance  */
    public V newItem();

    /**
     * returns the budget instance result if a new budget update is determined for item v.
     * also allow selector to modify anything else about the item besides budget
     * then if it returns non-null, reinsert
     * if this method simply returns null it means it has not changed the item
     *
     * @param result will be intialized to v's original budget value
     * */
    Budget updateItem(V v, Budget result);




    /** called when a bag operation produces an overflow (displaced item) */
    default void overflow(V overflow) {
        //System.err.println(this + " unhandled overflow: " + overflow);
        //new Exception().printStackTrace();
        overflow.delete();
    }


}
