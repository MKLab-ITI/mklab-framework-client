package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.WebPage;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.client.search.Query;
import gr.iti.mklab.framework.client.search.SearchEngineResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;


/**
 *
 * @author etzoannos
 */
public class SolrWebPageHandler {

    
    private SolrServer server;
	private Logger logger;
    private static Map<String, SolrWebPageHandler> INSTANCES = new HashMap<String, SolrWebPageHandler>();

    //private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    private static int commitPeriod = 5000;  // 5 SECONDS
    
    // Private constructor prevents instantiation from other classes
    private SolrWebPageHandler(String collection) {
        try {
        	logger = Logger.getRootLogger();
            server = new HttpSolrServer(collection);
        } catch (Exception e) {
            Logger.getRootLogger().info(e.getMessage());
        }
    }

    // implementing Singleton pattern
    public static SolrWebPageHandler getInstance(String collection) {
    	SolrWebPageHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrWebPageHandler(collection);
            INSTANCES.put(collection, INSTANCE);
        }
        
        return INSTANCE;
    }

    public boolean insertWebPage(WebPage webPage) {

        boolean status = true;
        try {
            SolrWebPage solrWebPage = new SolrWebPage(webPage);
            server.addBean(solrWebPage, commitPeriod);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            status = false;
        }
        
        return status;
    }

    public boolean insertWebPages(List<WebPage> webPages) {

        boolean status = true;
        try {
            List<SolrWebPage> solrWebPages = new ArrayList<SolrWebPage>();
            for (WebPage webPage : webPages) {
            	SolrWebPage solrWebPage = new SolrWebPage(webPage);
            	solrWebPages.add(solrWebPage);
            }

            server.addBeans(solrWebPages, commitPeriod);

        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            status = false;
        }
            
        return status;
    }

    public SearchEngineResponse<WebPage> addFilterAndSearchItems(Query query, String fq) {
        SolrQuery solrQuery = new SolrQuery(query.getQueryString());
        solrQuery.addFilterQuery(fq);
        return search(solrQuery);
    }

    public SearchEngineResponse<WebPage> removeFilterAndSearchItems(Query query, String fq) {
        SolrQuery solrQuery = new SolrQuery(query.getQueryString());
        return removeFilterAndSearch(solrQuery, fq);
    }

    public boolean deleteWebPage(String url) {
        boolean status = false;
        try {
        
        	String query = "url:" + url;
        	UpdateResponse response = server.deleteByQuery(query, commitPeriod);
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

    public boolean deleteWebPages(Query query) {
        boolean status = false;
        try {
        	UpdateResponse response = server.deleteByQuery(query.getQueryString(), commitPeriod);
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
    
    private SearchEngineResponse<WebPage> removeFilterAndSearch(SolrQuery query, String fq) {
        query.removeFilterQuery(fq);
        return search(query);
    }

    public WebPage getSolrWebPage(String url) {

        SolrQuery solrQuery = new SolrQuery("url:" + url);
        SearchEngineResponse<WebPage> response = search(solrQuery);
        List<WebPage> results = response.getResults();

        if (results == null || results.isEmpty()) {
            return null;
        }

        WebPage webPage = results.get(0);
        return webPage;
    }

    private SearchEngineResponse<WebPage> search(SolrQuery query) {

        SearchEngineResponse<WebPage> response = new SearchEngineResponse<WebPage>();
        try {
        	QueryResponse rsp = server.query(query);
            List<SolrWebPage> solrWebPages = rsp.getBeans(SolrWebPage.class);
            
            List<WebPage> webPages = new ArrayList<WebPage>();
            for (SolrWebPage solrWebPage : solrWebPages) {
                try {
                	WebPage webPage = solrWebPage.toWebPage();            	
                	webPages.add(webPage);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            
            response.setResults(webPages);
            
        } catch (SolrServerException e) {
        	logger.info(e.getMessage());
        }

        return response;
    }
    
    public List<WebPage> findWebPages(Dysco dysco, List<String> filters, List<String> facets, int size) {
  
        List<WebPage> webPages = new ArrayList<WebPage>();
        
        List<gr.iti.mklab.framework.common.domain.Query> queries = dysco.getSolrQueries();
        if (queries == null || queries.isEmpty()) {
            return webPages;
        }

        // Retrieve web pages from solr index
        Set<String> uniqueUrls = new HashSet<String>();
        Set<String> expandedUrls = new HashSet<String>();
        Set<String> titles = new HashSet<String>();

        String query = Utils.buildKeywordSolrQuery(queries, "OR");
        query = "((title : (" + query + ")) OR (text:(" + query + "))";
      
        //Set source filters in case they exist exist
        for (String filter : filters) {
            query += " AND " + filter;
        }
        
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(size);
        
        solrQuery.addSort("score", ORDER.desc);
        solrQuery.addSort("date", ORDER.desc);

        
      //Set facets if necessary
        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);
        }

        
        logger.info("Query : " + solrQuery);
        
        SearchEngineResponse<WebPage> response = search(solrQuery);
        if (response != null) {
            List<WebPage> results = response.getResults();
            for (WebPage webPage : results) {
                
            	String url = webPage.getUrl();
                String expandedUrl = webPage.getExpandedUrl();
                String title = webPage.getTitle();
                
                if (!expandedUrls.contains(expandedUrl) && !uniqueUrls.contains(url) && !titles.contains(title)) {
                    webPages.add(webPage);
                    
                    uniqueUrls.add(url);
                    expandedUrls.add(expandedUrl);
                    titles.add(title);
                }
            }
        }
        
        return webPages.subList(0, Math.min(webPages.size(), size));
    }
    
    public static void main(String...args) {
    	
    }
    
}