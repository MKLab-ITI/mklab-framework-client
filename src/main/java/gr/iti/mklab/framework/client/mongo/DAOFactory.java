package gr.iti.mklab.framework.client.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;

import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.RssFeed;

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
		BasicDAO<Feed, String> dao = factory.getDAO("xxx.xxx.xxx.xxx", "UKGeneralElections", Feed.class);
		
		Long since = System.currentTimeMillis()- 90*24*3600*1000L;
		
		Feed feed1 = new RssFeed("1", "http://www.telegraph.co.uk/news/general-election-2015/rss/", since, "RSS");
		feed1.setLabel("GE");

		Feed feed2 = new RssFeed("2", "http://www.ft.com/rss/indepth/uk-general-election/", since, "RSS");
		feed2.setLabel("GE");
		
		Feed feed3 = new RssFeed("3", "http://www.theguardian.com/politics/general-election-2015/rss", since, "RSS");
		feed3.setLabel("GE");
		
		Feed feed4 = new RssFeed("4", "http://www.thetimes.co.uk/tto/news/politics/rss", since, "RSS");
		feed4.setLabel("NonGE");
		 
		Feed feed5 = new RssFeed("5", "http://www.independent.co.uk/news/uk/politics/generalelection/?service=rss", since, "RSS");
		feed5.setLabel("GE");	
		
		Feed feed6 = new RssFeed("6", "http://www.standard.co.uk/news/politics/rss", since, "RSS");
		feed6.setLabel("NonGE");
		
		Feed feed7 = new RssFeed("7", "http://metro.co.uk/tag/general-election-2015/feed/", since, "RSS");
		feed7.setLabel("GE");
		
		Feed feed8 = new RssFeed("8", "http://www.scotsman.com/rss/cmlink/1.3716909", since, "RSS");
		feed8.setLabel("GE");
		
		Feed feed9 = new RssFeed("9", "http://feeds.bbci.co.uk/news/election/2015/rss.xml", since, "RSS");
		feed9.setLabel("GE");
		
		Feed feed10 = new RssFeed("10", "http://www.channel4.com/news/politics/rss ", since, "RSS");
		feed10.setLabel("NonGE");
		
		Feed feed11 = new RssFeed("11", "http://feeds.skynews.com/feeds/rss/politics.xml", since, "RSS");
		feed11.setLabel("NonGE");
			
		Feed feed12 = new RssFeed("12", "http://www.economist.com/topics/british-elections/index.xml", since, "RSS");
		feed12.setLabel("GE");
		
		Feed feed13 = new RssFeed("13", "http://www.spectator.co.uk/tag/general-election-2015/feed/", since, "RSS");
		feed13.setLabel("GE");
		
		Feed feed14 = new RssFeed("14", "http://may2015.com/feed/", since, "RSS");
		feed14.setLabel("GE");
		
		Feed feed15 = new RssFeed("15", "http://www.buzzfeed.com/tag/ge2015.xml", since, "RSS");
		feed15.setLabel("GE");
		
		Feed feed16 = new RssFeed("16", "www.huffingtonpost.co.uk/news/general-election-2015/feed/", since, "RSS");
		feed16.setLabel("GE");
		
		Feed feed17 = new RssFeed("17", "http://feeds2.feedburner.com/guidofawkes", since, "RSS");
		feed17.setLabel("GE");
		
		Feed feed18 = new RssFeed("18", "http://labourlist.org/category/news/feed/", since, "RSS");
		feed18.setLabel("GE");
		
		Feed feed19 = new RssFeed("19", "https://www.politicshome.com/rss.xml", since, "RSS");
		feed19.setLabel("GE");		
				
		dao.save(feed1);
		dao.save(feed2);
		dao.save(feed3);
		dao.save(feed4);
		dao.save(feed5);
		dao.save(feed6);
		dao.save(feed7);
		dao.save(feed8);
		dao.save(feed9);
		dao.save(feed10);
		dao.save(feed11);
		dao.save(feed12);
		dao.save(feed13);
		dao.save(feed14);
		dao.save(feed15);
		dao.save(feed16);
		dao.save(feed17);
		dao.save(feed18);
		dao.save(feed19);
	}
	
}
