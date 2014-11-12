package gr.iti.mklab.framework.client.dao.impl;

import gr.iti.mklab.framework.common.domain.JSONable;
import gr.iti.mklab.framework.common.domain.WebPage;
import gr.iti.mklab.framework.common.factories.ObjectFactory;
import gr.iti.mklab.framework.client.dao.WebPageDAO;
import gr.iti.mklab.framework.client.mongo.MongoHandler;
import gr.iti.mklab.framework.client.mongo.Selector;
import gr.iti.mklab.framework.client.mongo.UpdateItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author etzoannos - e.tzoannos@atc.gr
 */
public class WebPageDAOImpl implements WebPageDAO {

    private MongoHandler mongoHandler;
    private final static String db = "Streams";
    private final static String collection = "WebPages";
    private List<String> indexes = new ArrayList<String>();

    public WebPageDAOImpl(String host) throws Exception {
        this(host, db, collection);
    }

    public WebPageDAOImpl(String host, String db, String collection) throws Exception {
        indexes.add("url");
        indexes.add("references");
        
        mongoHandler = new MongoHandler(host, db, collection, indexes);
        
    }

    @Override
    public void addWebPage(WebPage webPage) {
        mongoHandler.insert(webPage);
    }

    @Override
    public WebPage getWebPage(String webPageURL) {
        String resultString = mongoHandler.findOne("url", webPageURL);
        WebPage result = ObjectFactory.createWebPage(resultString);
        return result;
    }

    @Override
    public int getWebPageShares(String webPageURL) {
        Object result = mongoHandler.findOneField("url", webPageURL, "shares");
        if(result == null)
        	return 0;
        
        return (Integer) result;
    }
    
    @Override
    public List<WebPage> getLastWebPages(int size) {
        List<String> jsonWebPages = mongoHandler.findMany(new Selector(), size);
        List<WebPage> results = new ArrayList<WebPage>();
        for (String json : jsonWebPages) {
            results.add(ObjectFactory.createWebPage(json));
        }
        return results;
    }

    @Override
    public List<WebPage> getWebPagesForTweets(List<String> tweetIds) {

        List<String> jsonWebPages = mongoHandler.findManyWithOr("reference", tweetIds, 20);

        List<WebPage> results = new ArrayList<WebPage>();
        for (String json : jsonWebPages) {
            results.add(ObjectFactory.createWebPage(json));
        }
        return results;
    }

    @Override
    public List<WebPage> getWebPagesForUrls(List<String> urls) {

        List<String> jsonWebPages = mongoHandler.findManyWithOr("url", urls, 20);

        List<WebPage> results = new ArrayList<WebPage>();
        for (String json : jsonWebPages) {
            results.add(ObjectFactory.createWebPage(json));
        }
        
        // remove duplicates from pages
        
        Map<String,WebPage> map = new HashMap<String,WebPage>();
        
        for (WebPage page: results) {
            map.put(page.getUrl(), page);
        }
                
        results.clear();
        results.addAll(map.values());
        
        return results;
    }

    @Override
    public void removeWebPage(String webPageURL) {
        mongoHandler.delete("url", webPageURL);
    }

    @Override
    public void clearAll() {
        mongoHandler.clean();
    }

    @Override
    public void updateWebPage(String webPageURL, String name, Object value) {
        UpdateItem changes = new UpdateItem();
        changes.setField(name, value);
        mongoHandler.update("url", webPageURL, changes);

    }

    @Override
    public void updateWebPage(String webPageURL, JSONable changes) {
        mongoHandler.update("url", webPageURL, changes);
    }

    @Override
    public void updateWebPageShares(String webPageURL) {
    	updateWebPageShares(webPageURL, 1);
    }

    @Override
    public void updateWebPageShares(String webPageURL, int shares) {
        UpdateItem update = new UpdateItem();
        update.incField("shares", shares);
        mongoHandler.update("url", webPageURL, update);
    }
    
	@Override
	public boolean exists(String webPageURL) {
		return mongoHandler.exists("url", webPageURL);
	}

	@Override
	public List<WebPage> getWebPages(Selector query, int size) {
		List<String> jsonWebPages = mongoHandler.findMany(query, size);
		
		List<WebPage> results = new ArrayList<WebPage>();
        for (String json : jsonWebPages) {
            results.add(ObjectFactory.createWebPage(json));
        }
        
        return results;
	}

}
