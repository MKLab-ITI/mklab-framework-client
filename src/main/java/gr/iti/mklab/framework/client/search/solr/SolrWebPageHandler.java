package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.SearchResponse;
import gr.iti.mklab.framework.client.search.solr.beans.WebPageBean;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author Manos Schinas
 * @email  manosetro@iti.gr
 * 
 */
public class SolrWebPageHandler extends SolrHandler<WebPageBean> {

	private static Map<String, SolrWebPageHandler> INSTANCES = new HashMap<String, SolrWebPageHandler>();
    
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

    public static SolrWebPageHandler getInstance(String service, String collection) {
    	if(service.endsWith("/")) {
    		return getInstance(service + collection);
    	}
    	else {
    		return getInstance(service + "/" + collection);
    	}
    }

    public SearchResponse<WebPageBean> find(SolrQuery query) {

    	SearchResponse<WebPageBean> response = new SearchResponse<WebPageBean>();
        try {
        	QueryResponse rsp = server.query(query);
        	
            List<WebPageBean> wpBeans = rsp.getBeans(WebPageBean.class);
            response.setResults(wpBeans);
            
        } catch (SolrServerException e) {
        	logger.info(e.getMessage());
        }

        return response;
    }
    
    public List<String> findWebPages(String textQuery, List<String> filters, List<String> facetsFields, int size) {
    	  
        List<String> webPages = new ArrayList<String>();

        StringBuffer query = new StringBuffer();
        query.append("title : (" + textQuery + ") OR text:(" + textQuery + ")");
        
        //Set source filters in case they exist exist
        if(!filters.isEmpty()) {
        	String filterQuery = StringUtils.join(filters, " AND ");
        	if(query.length() == 0) {
        		query.append(filterQuery);
        	}
        	else {
        		query.append(" AND " + filterQuery);
        	}
        }
        
        SolrQuery solrQuery = new SolrQuery(query.toString());
        solrQuery.setRows(size);
        
        solrQuery.addSort("score", ORDER.desc);
        solrQuery.addSort("date", ORDER.desc);

        
      //Set facets if necessary
        for (String facetField : facetsFields) {
            solrQuery.addFacetField(facetField);
            solrQuery.setFacetLimit(10);
        }
        
        logger.info("Query : " + solrQuery);
        
        SearchResponse<WebPageBean> response = find(solrQuery);
        if (response != null) {
            List<WebPageBean> results = response.getResults();
            for (WebPageBean webPage : results) {
            	webPages.add(webPage.getUrl());
            }
        }
        
        return webPages.subList(0, Math.min(webPages.size(), size));
    }
    
    public List<String> findWebPages(Dysco dysco, List<String> filters, List<String> facets, int size) {
  
        Set<String> urls = new HashSet<String>();

        String query = "";
        Map<String, String> keywords = dysco.getKeywords();
        if(keywords != null && !keywords.isEmpty()) {
        	query = StringUtils.join(keywords.keySet(), "OR");
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
        List<String> keywordsToExclude = dysco.getKeywordsToExclude();
        if (keywordsToExclude != null && !keywordsToExclude.isEmpty()) {
        	String excludeQuery = StringUtils.join(keywordsToExclude, " OR ");
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
        
        SearchResponse<WebPageBean> response = find(solrQuery);
        for(WebPageBean wpBean : response.getResults()) {
        	urls.add(wpBean.getUrl());
        }
        
        return new ArrayList<String>(urls);
    }
      
}