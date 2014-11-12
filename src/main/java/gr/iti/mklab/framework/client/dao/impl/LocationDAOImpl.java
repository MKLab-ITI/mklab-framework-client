package gr.iti.mklab.framework.client.dao.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import gr.iti.mklab.framework.client.dao.LocationDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;

public class LocationDAOImpl implements LocationDAO {

	List<String> indexes = new ArrayList<String>();
    private static String db = "Streams";
    private static String collection = "	Locations";
    private MongoHandler mongoHandler;

    public LocationDAOImpl(String host) {
    	this(host, db, collection);
    }
    
    public LocationDAOImpl(String host, String db, String collection) {
        
        try {
			mongoHandler = new MongoHandler(host, db, collection, indexes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   
    }
    
	@Override
	public void insertLocation(String name, double latitude, double longitude) {
		Location location = new Location(latitude, longitude, name);
		
		Map<String, Object> map = location.toJSONMap();
		map.put("_id", name);
		map.put("timestamp", System.currentTimeMillis());
		
		mongoHandler.update("_id", name, map);
	}

	@Override
	public void insertLocation(Location location, SocialNetworkSource sourceType) {
		String id = sourceType+"::"+location.getName();
		Map<String, Object> map = location.toJSONMap();
		map.put("_id", id);
		map.put("timestamp", System.currentTimeMillis());
		map.put("source", sourceType);
		mongoHandler.update("_id", id, map);
	}
	
	@Override
	public void removeLocation(Location location) {
		String name = location.getName();
		if(name != null) {
			mongoHandler.delete("name", name);
		}
	}

	@Override
	public void removeLocation(Location location, SocialNetworkSource sourceType) {
		
		String name = location.getName();
		if(name != null) {
			if(sourceType == SocialNetworkSource.All) {
				 mongoHandler.delete("name", name);
	        }
			else {
				Map<String, Object> map = new HashMap<String, Object>();
		        map.put("name", name);
		        map.put("source", sourceType);
		        mongoHandler.delete(map);
			}
    	
		}
		
	}



}
