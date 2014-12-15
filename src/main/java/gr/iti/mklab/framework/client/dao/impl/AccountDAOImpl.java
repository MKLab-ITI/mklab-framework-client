package gr.iti.mklab.framework.client.dao.impl;

import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.AccountDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author etzoannos
 */
public class AccountDAOImpl implements AccountDAO {

    List<String> indexes = new ArrayList<String>();

    private static String db = "test";
    private static String collection = "sources";
    private MongoHandler mongoHandler;

    
    public AccountDAOImpl(String host) {
    	this(host, db, collection);
    }

    public AccountDAOImpl(String host, String db, String collection) {
        try {
            indexes.add("score");
            indexes.add("id");
            indexes.add("name");
            mongoHandler = new MongoHandler(host, db, collection, indexes);
            
            mongoHandler.sortBy("score", 1);
        } catch (Exception ex) {
            org.apache.log4j.Logger.getRootLogger().error(ex.getMessage());
        }
    }
    
    @Override
    public void removeAccount(Account source) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", source.getName());
        mongoHandler.delete(map);
    }

    @Override
	public void removeAccount(Account source, Source sourceType) {
    	Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", source.getName());
        if(sourceType != Source.All) {
        	map.put("network", sourceType.name());
        }
        mongoHandler.delete(map);
	}
    
    @Override
    public void insertAccount(String name, float score) {
        Map<String, Object> map = new HashMap<String, Object>();
        String id = Source.All+"::"+name;
        map.put("_id", id);
        map.put("name", name);
        map.put("score", score);
        map.put("timestamp", System.currentTimeMillis());
        if(mongoHandler.exists("_id", id))
        	mongoHandler.update("_id", id, map);
        else
        	mongoHandler.insert(map);
        
    }

    @Override
    public void insertAccount(Account source) {
        Map<String, Object> map = new HashMap<String, Object>();
        String id = Source.All+"::"+source.getName();
        map.put("_id", id);
        map.put("id", source.getId());
        map.put("name", source.getName());
        map.put("score", source.getScore());
        map.put("list", source.getList());
        map.put("timestamp", System.currentTimeMillis());
        if(mongoHandler.exists("_id", id))
        	mongoHandler.update("_id", id, map);
        else
        	mongoHandler.insert(map);

    }
    
    @Override
	public void insertAccount(String name, float score, Source snSource) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	String id = snSource.toString()+"::"+name;
        map.put("_id", id);
        map.put("name", name);
        map.put("score", score);
        map.put("network", snSource.toString());
        map.put("timestamp", System.currentTimeMillis());
        if(mongoHandler.exists("_id", id))
        	mongoHandler.update("_id", id, map);
        else
        	mongoHandler.insert(map);
    }

    @Override
	public void insertAccount(Account source, Source sourceType) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	String id = sourceType.toString()+"::"+source.getName();
        map.put("_id", id);
        map.put("id", source.getId());
        map.put("name", source.getName());
        map.put("score", source.getScore());
        map.put("list", source.getList());
        map.put("network", sourceType.name());
        map.put("timestamp", System.currentTimeMillis());
        if(mongoHandler.exists("_id", id))
        	mongoHandler.update("_id", id, map);
        else
        	mongoHandler.insert(map);
    }
    
    @Override
    public void instertDyscoAccount(String dyscoId, String name, float score) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("score", score);
        map.put("dyscoId", dyscoId);
        mongoHandler.insert(map);
    }

	@Override
	public List<Account> findTopAccounts(int n) {
		List<Account> sources = new ArrayList<Account>();
		
		List<String> res = mongoHandler.findMany(n);
		for(String json : res) {
			sources.add(ObjectFactory.createAccount(json));
		}
		return sources;
	}
	
	@Override
	public List<Account> findTopAccounts(int n, Source sourceType) {
		List<Account> sources = new ArrayList<Account>();
		
		List<String> res = mongoHandler.findMany("network", sourceType.toString(), n);
		for(String json : res) {
			sources.add(ObjectFactory.createAccount(json));
		}
		return sources;
	}

	@Override
	public List<Account> findAllAccounts() {
		return findTopAccounts(-1);
	}

	@Override
	public List<Account> findListAccounts(String listId) {
		List<Account> sources = new ArrayList<Account>();
		
		List<String> res = mongoHandler.findMany("list", listId, -1);
		for(String json : res) {
			sources.add(ObjectFactory.createAccount(json));
		}
		return sources;
	}
	
}
