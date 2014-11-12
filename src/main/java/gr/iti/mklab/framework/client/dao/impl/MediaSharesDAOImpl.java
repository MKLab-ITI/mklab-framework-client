package gr.iti.mklab.framework.client.dao.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import gr.iti.mklab.framework.common.domain.MediaShare;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.MediaSharesDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;

public class MediaSharesDAOImpl implements MediaSharesDAO {

	private List<String> indexes = new ArrayList<String>();
	
	private MongoHandler mongoHandler = null;
	
	public MediaSharesDAOImpl(String host, String db, String collection) {
        indexes.add("reference");
        indexes.add("publicationTime");

        try {
            mongoHandler = new MongoHandler(host, db, collection, indexes);
            mongoHandler.sortBy("publicationTime", MongoHandler.DESC);
        } catch (UnknownHostException ex) {
            Logger.getLogger(ItemDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

	
	@Override
	public void addMediaShare(String id, String originalId,
			long publicationTime, String userid) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("reference", originalId);
		map.put("publicationTime", publicationTime);
		map.put("userid", userid);
		
		mongoHandler.insert(map);
	}

	@Override
	public List<MediaShare> getMediaShares(String mediaId) {
		
		DBObject query = new BasicDBObject("id", mediaId);
		List<String> response = mongoHandler.findMany(query , -1);
		
		List<MediaShare> mediaShares = new ArrayList<MediaShare>();
		for(String json : response) {
			MediaShare ms = ObjectFactory.createMediaShare(json);
			mediaShares.add(ms);
		}
		return mediaShares;
	}

}
