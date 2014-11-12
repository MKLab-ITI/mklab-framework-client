package gr.iti.mklab.framework.client.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.StreamUserDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;
import gr.iti.mklab.framework.client.mongo.Selector;
import gr.iti.mklab.framework.client.mongo.UpdateItem;

import org.apache.commons.lang.StringEscapeUtils;

public class StreamUserDAOImpl implements StreamUserDAO {

    List<String> indexes = new ArrayList<String>();
    private final static String db = "Streams";
    private final static String collection = "StreamUsers";
    private MongoHandler mongoHandler;

    public StreamUserDAOImpl(String host) throws Exception {
        this(host, db, collection);
    }

    public StreamUserDAOImpl(String host, String db, String collection) throws Exception {
        indexes.add("id");
        indexes.add("userid");
        indexes.add("username");
        
        mongoHandler = new MongoHandler(host, db, collection, indexes);
        
    }

    @Override
    public void insertStreamUser(StreamUser user) {
        mongoHandler.insert(user);
    }

    @Override
    public boolean exists(String id) {
        return mongoHandler.exists("id", id);
    }

    @Override
    public void updateStreamUserOld(StreamUser user) {
        Logger.getRootLogger().info("updating stream user old");
        mongoHandler.updateOld("id", user.getId(), user);
    }

    @Override
    public void updateStreamUser(StreamUser user) {
        Logger.getRootLogger().info("updating stream user new");
        mongoHandler.update("id", user.getId(), user);
    }

    @Override
    public void updateStreamUserPopularity(StreamUser user) {
        String description = user.getDescription();
        if (description != null) {
            UpdateItem changes = new UpdateItem();
            changes.setField("description", description);
            mongoHandler.update("id", user.getId(), changes);
        }
    }

    @Override
    public void updateStreamUserStatistics(StreamUser user) {
        if (user != null) {
        	DBObject incs = new BasicDBObject();
        	boolean update = false;
        	if(user.getShares()>0) {
        		update = true;
        		incs.put("shares", user.getShares());
        	}
        	if(user.getMentions()>0) {
        		update = true;
        		incs.put("mentions", user.getMentions());
        	}
        	
        	if(user.getItems()>0) {
        		update = true;
        		incs.put("items", user.getItems());
        	}
        	
    		BasicDBObject sets = new BasicDBObject();
    		if(user.getImageUrl() != null)
    			sets.put("imageUrl", user.getImageUrl());
    		if(user.getProfileImage() != null)
    			sets.put("profileImage", user.getProfileImage());
    		if(user.getName() != null) 
    			sets.put("name", user.getName());
    		if(user.getLastUpdated() != null && user.getLastUpdated() > 0) 
    			sets.put("lastUpdated", user.getLastUpdated());
    		
        	if(update) {
        		DBObject change = new BasicDBObject("$inc", incs);
        		if(!sets.isEmpty()) {
        			change.put("$set", sets);
        		}
        		
        		mongoHandler.update("id", user.getId(), change);
        	}
        }
    }
    
    @Override
    public boolean deleteStreamUser(String id) {
        return mongoHandler.delete("userid", id);
    }

    @Override
    public StreamUser getStreamUser(String id) {
        String json = mongoHandler.findOne("id", id);
        StreamUser user = ObjectFactory.createUser(json);
        return user;
    }

    @Override
    public StreamUser getStreamUserByName(String username) {

        Selector query = new Selector();
        query.select("username", StringEscapeUtils.escapeHtml(username));
        query.select("streamId", "Twitter");

        String json = mongoHandler.findOne(query);

        StreamUser user = ObjectFactory.createUser(json);
        return user;
    }

    @Override
    public void incStreamUserValue(String userid, String field) {
    	incStreamUserValue(userid, field, 1);
    }

	@Override
	public void incStreamUserValue(String userid, String field, int value) {
        UpdateItem changes = new UpdateItem();
        changes.incField(field, value);
        mongoHandler.update("id", userid, changes);
	}
	
    @Override
    public Map<String, StreamUser> getStreamUsers(List<String> ids) {
        Map<String, StreamUser> users = new HashMap<String, StreamUser>();

        DBObject query = new BasicDBObject("id", new BasicDBObject("$in", ids));;

        List<String> response = mongoHandler.findMany(query, ids.size());
        for (String json : response) {
            StreamUser user = ObjectFactory.createUser(json);
            users.put(user.getId(), user);
        }

        return users;
    }
	
}
