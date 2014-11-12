package gr.iti.mklab.framework.client.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.mongo.MongoHandler.MongoIterator;

/**
 * Data Access Object for Item
 *
 * @author etzoannos
 */
public interface StreamUserDAO {

    public void insertStreamUser(StreamUser user);

    public void updateStreamUser(StreamUser user);

    public void updateStreamUserOld(StreamUser user);

    public void updateStreamUserPopularity(StreamUser user);

    public void updateStreamUserStatistics(StreamUser user);
    
    public void incStreamUserValue(String id, String field);
    
    public void incStreamUserValue(String id, String field, int value);
    
    public boolean deleteStreamUser(String id);

    public StreamUser getStreamUser(String id);
    
    public Map<String, StreamUser> getStreamUsers(List<String> ids);

    public StreamUser getStreamUserByName(String username);
    
    public boolean exists(String id);

    public class StreamUserIterator implements Iterator<StreamUser> {

		private MongoIterator it;

		public StreamUserIterator (MongoIterator it) {
    		this.it = it;
    	}
		
    	public StreamUser next() {
    		String json = it.next();
    		return ObjectFactory.createUser(json);
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
