package gr.iti.mklab.framework.client.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;

import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 *
 * @author Manos Schinas
 * @email  manosetro@iti.gr
 *
 */
public class DAOFactory {

    public static int ASC = 1;
    public static int DESC = -1;
    
    private static MongoClientOptions options = MongoClientOptions.builder()
            .writeConcern(WriteConcern.UNACKNOWLEDGED).build();
    
    private static Map<String, MongoClient> connections = new HashMap<String, MongoClient>();
    private static Map<String, Datastore> datastores = new HashMap<String, Datastore>();

    public <K> BasicDAO<K, String> getDAO(String hostname, String dbName, Class<K> clazz) throws Exception {
    	
        String connectionKey = hostname + "#" + dbName;
        Datastore ds = datastores.get(connectionKey);
        
        Morphia morphia = new Morphia();
        MongoClient mongoClient = connections.get(hostname);

        if (mongoClient == null) {
        	mongoClient = new MongoClient(hostname, options);
            connections.put(hostname, mongoClient);
        }
        
        if (ds == null) {
            ds = morphia.createDatastore(mongoClient, dbName);
            datastores.put(connectionKey, ds);
        }
		
        return new BasicDAO<K, String>(clazz, mongoClient, morphia, dbName);
    }

	public static void main(String...args) throws Exception {
		DAOFactory factory = new DAOFactory();
		BasicDAO<Feed, String> dao = factory.getDAO("160.40.50.207", "test", Feed.class);
		
		Date since = new Date(System.currentTimeMillis()- 1*24*3600*1000);
		
		Feed feed1 = new AccountFeed("1", "MWC_Barcelona", since, "Twitter");
		
		Feed feed2 = new AccountFeed("2", "startups_bcn", since, "Twitter");

		Feed feed3 = new AccountFeed("3", "4YFN_MWC", since, "Twitter");
		
		Feed feed4 = new KeywordsFeed("4", "MWC15", since, "Twitter");
	
		//Feed feed5 = new KeywordsFeed("5", "#connectedbeings", since);
		//feed5.setSource("Twitter");
		
		//Feed feed6 = new KeywordsFeed("6", "#4YFN", since);
		//feed6.setSource("Twitter");
		
		//dao.save(feed1);
		//dao.save(feed2);
		//dao.save(feed3);
		dao.save(feed4);
		//dao.save(feed5);
		//dao.save(feed6);
		
		//Set<Feed> feeds = new HashSet<Feed>(dao.find().asList());	
		//Set<Feed> feeds2 = new HashSet<Feed>(dao.find().asList().subList(0, 4));		
		//feeds.removeAll(feeds2);
	}
	
}
