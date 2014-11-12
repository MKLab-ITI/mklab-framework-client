package gr.iti.mklab.framework.client.dao;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.mongo.MongoHandler.MongoIterator;

import java.util.Iterator;
import java.util.List;

/**
 * Data Access Object for Item
 *
 * @author etzoannos
 */
public interface ItemDAO {

    public void insertItem(Item item);

    public void replaceItem(Item item);

    public void updateItem(Item item);

    public boolean deleteItem(String id);

    public Item getItem(String id);

    public List<Item> getLatestItems(int n);

    public List<Item> getItemsSince(long date);

    public List<Item> getItemsInTimeslot(String timeslotId);

    boolean exists(String id);

    public List<Item> readItems();

    public List<Item> readItemsByStatus();

    public List<Item> getItemsInRange(long start, long end);

    public void setIndexedStatusTrue(String itemId);

    public List<Item> getUnindexedItems(int max);

    public class ItemIterator implements Iterator<Item> {

		private MongoIterator it;

		public ItemIterator (MongoIterator it) {
    		this.it = it;
    	}
		
    	public Item next() {
    		String json = it.next();
    		return ObjectFactory.create(json);
    	}
    	
    	public boolean hasNext() {
    		return it.hasNext();
    	}

		@Override
		public void remove() {
			it.next();
		}
    }

}
