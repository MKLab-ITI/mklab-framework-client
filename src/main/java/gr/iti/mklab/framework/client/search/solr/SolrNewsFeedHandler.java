package gr.iti.mklab.framework.client.search.solr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.Query;
import gr.iti.mklab.framework.client.search.SearchEngineResponse;

public class SolrNewsFeedHandler {
	 private Logger logger = Logger.getLogger(SolrNewsFeedHandler.class);
	    /*
	     CommonsHttpSolrServer is thread-safe and if you are using the following constructor,
	     you *MUST* re-use the same instance for all requests.  If instances are created on
	     the fly, it can cause a connection leak. The recommended practice is to keep a
	     static instance of CommonsHttpSolrServer per solr server url and share it for all requests.
	     See https://issues.apache.org/jira/browse/SOLR-861 for more details
	     */
	    SolrServer server;
	    private static Map<String, SolrNewsFeedHandler> INSTANCES = new HashMap<String, SolrNewsFeedHandler>();
	    private static int commitPeriod = 10000;

	    // Private constructor prevents instantiation from other classes
//	    private SolrItemHandler() {
//	        try {
////	            ConfigReader configReader = new ConfigReader();
////	            String url = configReader.getSolrHTTP();  
//	              Logger.getRootLogger().info("going to create SolrServer: " + ConfigReader.getSolrHome() + "/items");
//	        	server = new HttpSolrServer( ConfigReader.getSolrHome() + "/items");
////	            server = new HttpSolrServer("server/solr/DyscoMediaItems");
//	            /*
//	        	DefaultHttpClient httpclient = new DefaultHttpClient();
//	        	HttpHost proxy = new HttpHost(proxyName, port, "http");
//	        	httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
//	        	Credentials creds = new UsernamePasswordCredentials(username, password);
//	        	httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
//	        	server = new HttpSolrServer("http://social1.atc.gr:8080/solr/items", httpclient);*/
//	        } catch (Exception e) {
//	            Logger.getRootLogger().info(e.getMessage());
//	        }
//	    }
	    private SolrNewsFeedHandler(String collection) throws Exception{
	    
//	            ConfigReader configReader = new ConfigReader();
//	            String url = configReader.getSolrHTTP();    
	            server = new HttpSolrServer(collection);
	            
	            server.ping();
	            
	            //Logger.getRootLogger().info("going to create SolrServer: " + ConfigReader.getSolrHome() + "/DyscoMediaItems");
	            //server = new HttpSolrServer( ConfigReader.getSolrHome() + "/DyscoMediaItems");

	    }
	    
	    
	    public void checkServerStatus() throws Exception{
	    	server.ping();
	    }
	    
//	    //implementing Singleton pattern
//	    public static SolrItemHandler getInstance() {
//	        if (INSTANCE == null) {
//	            INSTANCE = new SolrItemHandler();
//	        }
//	        return INSTANCE;
//	    }
	    //implementing Singleton pattern
	    public static SolrNewsFeedHandler getInstance(String collection) throws Exception {
	    	SolrNewsFeedHandler INSTANCE = INSTANCES.get(collection);
	        if (INSTANCE == null) {
	            INSTANCE = new SolrNewsFeedHandler(collection);
	            INSTANCES.put(collection, INSTANCE);
	        }
	        return INSTANCE;
	    }

	    public boolean insertItem(Item item) {

	        boolean status = true;
	        try {
	            SolrNewsFeed solrNewsFeed = new SolrNewsFeed(item);

	            server.addBean(solrNewsFeed, commitPeriod);

	        } catch (SolrServerException ex) {
	            logger.error(ex.getMessage());
	            status = false;
	        } catch (Exception ex) {
	            status = false;
	        }
	        
	        return status;
	    }

	    public boolean insertItems(List<Item> items) {

	        boolean status = true;
	        try {
	            List<SolrNewsFeed> solrNewsFeeds = new ArrayList<SolrNewsFeed>();
	            for (Item item : items) {
	            	SolrNewsFeed solrNewsFeed = new SolrNewsFeed(item);
	            	solrNewsFeeds.add(solrNewsFeed);
	            }

	            server.addBeans(solrNewsFeeds, commitPeriod);

//	            UpdateResponse response = server.commit();
//	            int statusId = response.getStatus();
//	            if (statusId == 0) {
//	                status = true;
//	            }

	        } catch (SolrServerException ex) {
	            logger.error(ex.getMessage());
	            status = false;
	        } catch (IOException ex) {
	            logger.error(ex.getMessage());
	            status = false;
	        } 
	        return status;

	    }

	    public void forceCommitPending() {

	        try {

	            server.commit();
	        } catch (SolrServerException ex) {
	            logger.error(ex.getMessage());
	        } catch (IOException ex) {
	            logger.error(ex.getMessage());
	        }
	    }

	    public SearchEngineResponse<Item> addFilterAndSearchItems(Query query, String fq) {

	        SolrQuery solrQuery = new SolrQuery(query.getQueryString());
	        solrQuery.addFilterQuery(fq);
	        return search(solrQuery);
	    }

	    public SearchEngineResponse<Item> removeFilterAndSearchItems(Query query, String fq) {
	        SolrQuery solrQuery = new SolrQuery(query.getQueryString());
	        return removeFilterAndSearch(solrQuery, fq);
	    }

	    public boolean deleteItem(String itemId) {
	        boolean status = false;
	        try {
	            server.deleteByQuery("id:" + itemId);
	            UpdateResponse response = server.commit();
	            int statusId = response.getStatus();
	            if (statusId == 0) {
	                status = true;
	            }

	        } catch (SolrServerException ex) {
	            logger.error(ex.getMessage());
	        } catch (IOException ex) {
	            logger.error(ex.getMessage());
	        } 
	        
	        return status;
	    }

	    public boolean deleteItems(Query query) {
	        boolean status = false;
	        try {
	            server.deleteByQuery(query.getQueryString());
	            UpdateResponse response = server.commit();
	            int statusId = response.getStatus();
	            if (statusId == 0) {
	                status = true;
	            }

	        } catch (SolrServerException ex) {
	            logger.error(ex.getMessage());
	        } catch (IOException ex) {
	            logger.error(ex.getMessage());
	        } 
	        
	        return status;
	    
	    }
	    
	    public boolean deleteItemsOlderThan(long dateTime) {
	    	 boolean status = false;
	    	 try {
	             server.deleteByQuery("publicationTime : [* TO "+dateTime+"]");
//	             UpdateResponse response = server.commit();
//	             int statusId = response.getStatus();
//	             if (statusId == 0) {
//	                 status = true;
//	             }
	             
	             return true;

	         } catch (SolrServerException ex) {
	             logger.error(ex.getMessage());
	         } catch (IOException ex) {
	             logger.error(ex.getMessage());
	         } finally {
	             return status;
	         }
	    }

	    public SearchEngineResponse<Item> findItems(SolrQuery query) {

	        return search(query);
	    }

	    

	    public Item findItem(String itemId) {

	        SolrQuery solrQuery = new SolrQuery("id:\"" + itemId + "\"");
	        solrQuery.setRows(1);
	        SearchEngineResponse<Item> response = search(solrQuery);
	        List<Item> items = response.getResults();
	        if (!items.isEmpty()) {
	            return items.get(0);
	        } else {
	            Logger.getRootLogger().info("no tweet for this id found!!");
	            return null;
	        }
	    }
	    
	    public Item findLatestItem(){
	    	SolrQuery solrQuery = new SolrQuery("*:*");
	    	solrQuery.addSortField("publicationTime", SolrQuery.ORDER.desc);
	    	solrQuery.setRows(1);
	    	
	    	 SearchEngineResponse<Item> response = search(solrQuery);
	         List<Item> items = response.getResults();
	         if (!items.isEmpty()) {
	             return items.get(0);
	         } else {
	             Logger.getRootLogger().info("no solr found!!");
	             return null;
	         }
	    }
	    
	    private SearchEngineResponse<Item> addFilterAndSearch(SolrQuery query, String fq) {

	        query.addFilterQuery(fq);
	        return search(query);

	    }

	    private SearchEngineResponse<Item> removeFilterAndSearch(SolrQuery query, String fq) {

	        query.removeFilterQuery(fq);
	        return search(query);
	    }

	    private SearchEngineResponse<Item> search(SolrQuery query) {


	        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();

	        QueryResponse rsp;

	        System.out.println("query:  " + query.toString());
	        try {
	            rsp = server.query(query);
	        } catch (SolrServerException e) {
	            logger.info(e.getMessage());
	            return null;
	        }

	        response.setNumFound(rsp.getResults().getNumFound());

	        List<SolrItem> solrItems = rsp.getBeans(SolrItem.class);
	        if (solrItems != null) {
	            Logger.getRootLogger().info("got: " + solrItems.size() + " items from Solr - total results: " + response.getNumFound());
	        }


	        List<Item> items = new ArrayList<Item>();
	        for (SolrItem solrItem : solrItems) {
	            try {
	                items.add(solrItem.toItem());
	            } catch (MalformedURLException ex) {
	                logger.error(ex.getMessage());
	            }
	        }

	        response.setResults(items);
	        List<Facet> facets = new ArrayList<Facet>();
	        List<FacetField> solrFacetList = rsp.getFacetFields();
	        FacetField solrFacet;


	        if (solrFacetList != null) {

	            //populate all non-zero facets

	            for (int i = 0; i < solrFacetList.size(); i++) {

	                Facet facet = new Facet(); //initialize for Arcomem JSF UI
	                List<Bucket> buckets = new ArrayList<Bucket>();
	                solrFacet = solrFacetList.get(i); //get the ones returned from Solr
	                List<FacetField.Count> values = solrFacet.getValues();
	                String solrFacetName = solrFacet.getName();
	                boolean validFacet = false;

	                //populate Valid Facets
	                for (int j = 0; j < solrFacet.getValueCount(); j++) {

	                    Bucket bucket = new Bucket();
	                    long bucketCount = values.get(j).getCount();
//	                    if ((bucketCount > 0) && (bucketCount != solrItems.size())) { //bucket is neither non-zero length nor the whole set 
	                    validFacet = true; //facet contains at least one non-zero length bucket
	                    bucket.setCount(bucketCount);
	                    bucket.setName(values.get(j).getName());
	                    bucket.setQuery(values.get(j).getAsFilterQuery());
	                    bucket.setFacet(solrFacetName);
	                    buckets.add(bucket);
//	                    }
	                }
	                if (validFacet) { //add the facet only if it is contains at least one non-zero length - excludes the whole set result
	                    facet.setBuckets(buckets);
	                    facet.setName(solrFacetName);
	                    facets.add(facet);
	                }
	            }

	            Collections.sort(facets, new Comparator<Facet>() { //anonymous inner class used for sorting
	                @Override
	                public int compare(Facet f1, Facet f2) {

	                    String value1 = f1.getName();
	                    String value2 = f2.getName();

	                    if (value1.compareTo(value2) > 0) {
	                        return 1;
	                    } else {
	                        return -1;
	                    }
	                }
	            });
	        }
	        response.setFacets(facets);
	        return response;
	    }
}
