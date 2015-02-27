package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.WebPage;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.client.search.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
 * @author Manos Schinas
 * @email  manosetro@iti.gr
 * 
 */
public class SolrWebPageHandler implements SolrHandler<WebPage> {

	private static Map<String, SolrWebPageHandler> INSTANCES = new HashMap<String, SolrWebPageHandler>();
	
    private SolrServer server;
	private Logger logger;
   
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

    public boolean insert(WebPage webPage) {
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

    public boolean insert(List<WebPage> webPages) {
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

    public boolean deleteById(String url) {
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

    public boolean delete(String query) {
        boolean status = false;
        try {
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

    public SearchResponse<String> find(SolrQuery query) {

    	SearchResponse<String> response = new SearchResponse<String>();
        try {
        	QueryResponse rsp = server.query(query);
            List<SolrWebPage> solrWebPages = rsp.getBeans(SolrWebPage.class);
            
            List<String> webPages = new ArrayList<String>();
            for (SolrWebPage solrWebPage : solrWebPages) {
            	webPages.add(solrWebPage.getUrl());
            }
            
            response.setResults(webPages);
            
        } catch (SolrServerException e) {
        	logger.info(e.getMessage());
        }

        return response;
    }
    
    public List<String> findWebPages(Dysco dysco, List<String> filters, List<String> facets, int size) {
  
        List<String> webPages = new ArrayList<String>();

        String query = "";
        List<String> words = dysco.getWords();
        if(words != null && !words.isEmpty()) {
        	query = StringUtils.join(words, "OR");
        	query = "((title : (" + query + ")) OR (text:(" + query + "))";
        }
        
        //Set source filters in case they exist exist
        String filterQuery = StringUtils.join(filters, " AND");
        if(query.isEmpty()) {
        	query = filterQuery;
        }
        else {
        	query += " AND " + filterQuery;
        }
        
        //add words to exclude in query
        List<String> wordsToExclude = dysco.getWordsToExclude();
        if (wordsToExclude != null && !wordsToExclude.isEmpty()) {
        	String excludeQuery = StringUtils.join(wordsToExclude, " OR ");
        	query += " NOT (title : (" + excludeQuery + ") OR description:(" + excludeQuery + "))";
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
        
        SearchResponse<String> response = find(solrQuery);
        if (response != null) {
            List<String> results = response.getResults();
            for (String webPage : results) {
            	webPages.add(webPage);
            }
        }
        
        return webPages.subList(0, Math.min(webPages.size(), size));
    }
    
    public static void main(String...args) {
    	
    }
    
}