package gr.iti.mklab.framework.client.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;

import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
		BasicDAO<Feed, String> dao = factory.getDAO("xxx.xxx.xxx.xxx", "BBC", Feed.class);
		
		Long since = System.currentTimeMillis()- 30*24*3600*1000L;
		
		String[] usernames = {
			"dailymailuk", 
			"daily_express", 
			"thesunnewspaper", 
			"dailymirror", 
			"thescotsman", 
			"daily_record", 
			"itvnews", 
			"5_news", 
			"spectator", 
			"viceuk_news", 
			"yahoonewsuk", 
			"LabourList"
		};

		for(Integer i=0; i<usernames.length; i++) {
			
			Feed feed = new AccountFeed("Twitter#"+usernames[i], usernames[i], since, "Twitter");
			feed.setLabel("BBC");
			
			dao.save(feed);
		}
		
		String[] tags = {
			"#bbcbias",
			"#bbclies",
			"#itvbias",
			"#itvlies",
			"#skybias",
			"#skylies",
			"#channel4bias",
			"#channel4lies"
		};

		Feed hashtagsFeed = new KeywordsFeed("Twitter#hashtags", Arrays.asList(tags), since, "Twitter");
		hashtagsFeed.setLabel("BBC");
		
		dao.save(hashtagsFeed);
		
		int p = 5;
		List<String> ngrams = IOUtils.readLines(new FileInputStream("/home/manosetro/ngrams"));
		for(int i=0; i<(ngrams.size()/p); i++) {
			
			int fromIndex = i*p;
			int toIndex = Math.min((fromIndex+p), ngrams.size());
			
			List<String> keywords = ngrams.subList(fromIndex, toIndex);
		
			Feed feed = new KeywordsFeed("Twitter#"+i, keywords, since, "Twitter");
			feed.setLabel("BBC");
			
			dao.save(feed);
		}
		
		/*
		String[] urls = {
			"http://www.telegraph.co.uk/news/uknews/rss",
			"http://www.telegraph.co.uk/culture/tvandradio/rss",
			"http://www.telegraph.co.uk/finance/newsbysector/mediatechnologyandtelecoms/media/rss",
			"http://www.ft.com/rss/home/uk",
			"http://www.ft.com/rss/companies/media",
			"http://www.ft.com/rss/life-arts/film-television",
			"http://www.theguardian.com/uk/rss",
			"http://www.theguardian.com/media/rss",
			"http://www.theguardian.com/tv-and-radio/rss",
			"http://www.thetimes.co.uk/tto/news/uk/rss",
			"http://www.thetimes.co.uk/tto/news/medianews/rss",
			"http://www.thetimes.co.uk/tto/arts/tv-radio/rss",
			"http://www.independent.co.uk/news/uk/?service=rss",
			"http://www.independent.co.uk/news/media/?service=rss",
			"http://www.independent.co.uk/arts-entertainment/tv/?service=rss",
			"http://www.standard.co.uk/news/uk/rss",
			"http://www.standard.co.uk/business/media/rss",
			"http://www.standard.co.uk/showbiz/tv/rss",
			"http://metro.co.uk/news/uk/feed/",
			"http://metro.co.uk/entertainment/tv/feed/",
			"http://feeds.bbci.co.uk/news/uk/rss.xml",
			"http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml",
			"http://www.channel4.com/news/uk/rss",
			"http://www.channel4.com/news/culture/rss",
			"http://feeds.skynews.com/feeds/rss/uk.xml",
			"http://feeds.skynews.com/feeds/rss/entertainment.xml",
			"http://www.economist.com/topics/united-kingdom/index.xml",
			"http://www.economist.com/topics/media/index.xml",
			"http://www.newstatesman.com/feeds/topics/media.rss",
			"http://www.huffingtonpost.co.uk/news/news/feed/",
			"http://www.huffingtonpost.co.uk/news/media/feed/",
			"http://www.huffingtonpost.co.uk/news/uktv/feed/",
			"http://feeds2.feedburner.com/guidofawkes"
		};
		
		for(Integer i=0; i<urls.length; i++) {
		
			Feed feed = new RssFeed(i.toString(), urls[i], since, "RSS");
			feed.setLabel("BBC");
			
			dao.save(feed);
		}
		*/
		
	}
	
}
