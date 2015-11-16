package gr.iti.mklab.framework.client.search.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.SearchResponse;
import gr.iti.mklab.framework.client.search.solr.beans.ItemBean;
import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;

/**
 *
 * @author Manos Schinas
 */
public class SolrItemHandler extends SolrHandler<ItemBean> {
    
    private static Map<String, SolrItemHandler> INSTANCES = new HashMap<String, SolrItemHandler>();

    private SolrItemHandler(String collection) throws Exception {
    	try {
    		logger = Logger.getLogger(SolrItemHandler.class);
    		server = new HttpSolrServer(collection);
    	} catch (Exception e) {
    		logger.info(e.getMessage());
    	}
    }

    //implementing Singleton pattern
    public static SolrItemHandler getInstance(String collection) throws Exception {
        SolrItemHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrItemHandler(collection);

            INSTANCES.put(collection, INSTANCE);
        }
        return INSTANCE;
    }

    public static SolrItemHandler getInstance(String service, String collection) throws Exception {
    	if(service.endsWith("/")) {
    		return getInstance(service + collection);
    	}
    	else {
    		return getInstance(service + "/" + collection);
    	}
    }
    
    public SearchResponse<ItemBean> find(SolrQuery query) {

    	SearchResponse<ItemBean> response = new SearchResponse<ItemBean>();
        QueryResponse rsp;
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        }

        response.setNumFound(rsp.getResults().getNumFound());
        List<ItemBean> itemBeans = rsp.getBeans(ItemBean.class);
        if (itemBeans != null) {
            logger.info("got: " + itemBeans.size() + " items from Solr - total results: " + response.getNumFound());
        }
        response.setResults(itemBeans);

        List<Facet> facets = getFacets(rsp);
        response.setFacets(facets);

        return response;
    }
    
    public SearchResponse<ItemBean> findItems(String textQuery, List<String> filters, List<String> facetFields, 
    		String orderBy, int size) {

        if (textQuery == null || textQuery.isEmpty() || textQuery.equals("")) {
            return new SearchResponse<ItemBean>();
        }
        
        StringBuffer queryBuffer = new StringBuffer();
        queryBuffer.append("title:(" + textQuery + ") OR description:(" + textQuery + ")");

        SolrQuery solrQuery = new SolrQuery(queryBuffer.toString());
        solrQuery.setRows(size);

    	//Set source filters in case they exist exist
        if(filters != null && !filters.isEmpty()) {
        	String[] fq = filters.toArray(new String[filters.size()]);
        	solrQuery.setFilterQueries(fq);
        }
        
        //Set facets if necessary
        if(facetFields != null && !facetFields.isEmpty()) {
        	for (String facetField : facetFields) {
            	solrQuery.addFacetField(facetField);
        	}
        	solrQuery.setFacetLimit(10);
        }
        
        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        logger.info("Solr Query : " + solrQuery);
        
        SearchResponse<ItemBean> response = find(solrQuery);        
        return response;
    }

	/* */
    public SearchResponse<ItemBean> findItems(Dysco dysco, List<String> filters, List<String> facetFields, String orderBy, 
    		int size) {

    	SearchResponse<ItemBean> response = new SearchResponse<ItemBean>();    	 

        // Create a Solr Query
        List<String> queryParts = new ArrayList<String>();
        
        Map<String, String> keywords = dysco.getKeywords();
        if(keywords != null && !keywords.isEmpty()) {
        	String contentQuery = StringUtils.join(keywords.keySet(), " OR ");
        	if (contentQuery != null && !contentQuery.isEmpty()) {
        		queryParts.add("title : (" + contentQuery + ")");
        		queryParts.add("description : (" + contentQuery + ")");
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
        
        //Set filters in case they exist
        if(filters!=null && !filters.isEmpty()) {
        	String filtersQuery = StringUtils.join(filters, " AND ");
        	 if (query.isEmpty()) {
             	query = filtersQuery;
             } else {
             	query = "(" + query + ") AND " + filtersQuery;
             }
        }
        
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(size);

        //Set facets if necessary
        for (String facetField : facetFields) {
            solrQuery.addFacetField(facetField);
            solrQuery.setFacetLimit(10);
        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }
        logger.info("Solr Query: " + query);

        response = find(solrQuery);
        return response;
    }

    public static void main(String...args) throws Exception {
    	
    	String solrCollection = "http://xxx.xxx.xxx.xxx:8983/solr/Items";
    	SolrItemHandler solrHandler = SolrItemHandler.getInstance(solrCollection);
    	
    	System.out.println("Count: " + solrHandler.count("*:*"));
    
    }
    
}
