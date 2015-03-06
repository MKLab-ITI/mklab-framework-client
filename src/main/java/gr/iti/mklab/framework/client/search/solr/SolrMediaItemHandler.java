package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.SearchResponse;
import gr.iti.mklab.framework.client.search.solr.beans.MediaItemBean;
import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author	Manos Schinas
 * @email	manosetro@iti.gr
 * 
 */
public class SolrMediaItemHandler extends SolrHandler<MediaItemBean> {
    
	private static Map<String, SolrMediaItemHandler> INSTANCES = new HashMap<String, SolrMediaItemHandler>();

    // Private constructor prevents instantiation from other classes
    private SolrMediaItemHandler(String collection) throws Exception {
    	try {
    		logger = Logger.getLogger(SolrMediaItemHandler.class);
    		server = new HttpSolrServer(collection);
    	} catch (Exception e) {
    		logger.info(e.getMessage());
    	}
    }

    // implementing Singleton pattern
    public static SolrMediaItemHandler getInstance(String collection) throws Exception {
        SolrMediaItemHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrMediaItemHandler(collection);
            
            INSTANCES.put(collection, INSTANCE);
        }
        
        return INSTANCE;
    }

    public SearchResponse<MediaItemBean> find(SolrQuery query) {
    	
    	SearchResponse<MediaItemBean> response = new SearchResponse<MediaItemBean>();
        QueryResponse rsp;
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        }
        response.setNumFound(rsp.getResults().getNumFound());
        
        List<MediaItemBean> solrMediaItems = rsp.getBeans(MediaItemBean.class);
        if (solrMediaItems != null) {
            logger.info("got: " + solrMediaItems.size() + " media items from Solr - total results: " + response.getNumFound());
        }
        response.setResults(solrMediaItems);
        
        List<Facet> facets = getFacets(rsp);
        response.setFacets(facets);

        return response;
    }
    
    public SearchResponse<MediaItemBean> findMediaItems(String textQuery, List<String> filters, 
    		List<String> facetFields, String orderBy, int size) {

        SearchResponse<MediaItemBean> response = new SearchResponse<MediaItemBean>();
        if (textQuery == null || textQuery.equals("")) {
            return response;
        }

        StringBuffer query = new StringBuffer();
        query.append("(title : " + query + ") OR (description:" + query + ")");
        
        //Set filters in case they exist exist
        for (String filter : filters) {
            query.append(" AND " + filter);
        }

        SolrQuery solrQuery = new SolrQuery(query.toString());
        solrQuery.setRows(size);

        for (String facetField : facetFields) {
            solrQuery.addFacetField(facetField);
            solrQuery.setFacetLimit(10);
        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }
        
        logger.info("Solr Query : " + query);

        response = find(solrQuery);
        return response;
    }

    public SearchResponse<MediaItemBean> findMediaItems(Dysco dysco, List<String> filters, List<String> facets, String orderBy, int size) {

        SearchResponse<MediaItemBean> response = new SearchResponse<MediaItemBean>();

        // Create Query
        List<String> queryParts = new ArrayList<String>();
        
        Map<String, String> keywords = dysco.getKeywords();
        if(keywords != null && !keywords.isEmpty()) {
        String contentQuery = StringUtils.join(keywords.keySet(), " OR ");
        	if (contentQuery != null && !contentQuery.isEmpty()) {
        		queryParts.add("(title : (" + contentQuery + ")");
        		queryParts.add("(description : (" + contentQuery + ")");
        	}
        }
        
        //set Users Query
        List<Account> accounts = dysco.getAccounts();
        if (accounts != null && !accounts.isEmpty()) {
        	List<String> uids = new ArrayList<String>();
        	for(Account account : accounts) {
        		uids.add(account.getId());
        	}
        	
            String usersQuery = StringUtils.join(uids, " OR ");
            if (usersQuery != null && !usersQuery.isEmpty()) {
            	queryParts.add("uid : (" + usersQuery + ")");
            }
        }
        
        if (queryParts.isEmpty()) {
            return response;
        }
        
        String query = StringUtils.join(queryParts, " OR ");

        //add words to exclude in query
        List<String> keywordsToExclude = dysco.getKeywordsToExclude();
        if (keywordsToExclude != null && !keywordsToExclude.isEmpty()) {
        	String exclude = StringUtils.join(keywordsToExclude, " OR ");
        	query += " NOT (title : (" + exclude + ") OR description:(" + exclude + "))";
        }
        
        //Set source filters in case they exist exist
        if(filters!=null && !filters.isEmpty()) {
        	String filtersQuery = StringUtils.join(filters, " AND ");
        	 if (query.isEmpty()) {
             	query = filtersQuery;
             } else {
             	query = "(" + query + ") AND " + filtersQuery;
             }
        }

        SolrQuery solrQuery = new SolrQuery(query);
        Logger.getRootLogger().info("Solr Query: " + query);

        solrQuery.setRows(size);

        for (String facet : facets) {
        	//solrQuery.addFacetQuery(query);
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);
        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        response = find(solrQuery);

        return response;
    }
    
}