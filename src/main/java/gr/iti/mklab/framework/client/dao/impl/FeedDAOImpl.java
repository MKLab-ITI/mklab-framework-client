package gr.iti.mklab.framework.client.dao.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.FeedDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;

public class FeedDAOImpl implements FeedDAO{
	
	 List<String> indexes = new ArrayList<String>();
	 private MongoHandler mongoHandler;
	 
	 public FeedDAOImpl(String host, String db, String collection) throws Exception{
		 indexes.add("id");
		 try {
	            mongoHandler = new MongoHandler(host, db, collection, indexes);
	     } catch (UnknownHostException ex) {
	            Logger.getRootLogger().error(ex.getMessage());
	     }
	 }
	 
	 @Override
	 public void insertFeed(Feed feed){
		 mongoHandler.insert(feed);
	 }
	 
	 @Override
	 public boolean deleteFeed(Feed feed){
		 return mongoHandler.delete("id", feed.getId());
	 }
	 
	 @Override
	 public Feed getFeed(String id){
		 String json = mongoHandler.findOne("id", id);
		 Feed feed = ObjectFactory.createFeed(json);
		 return feed;
	 }
	 
}
