package gr.iti.mklab.framework.client.search.solr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;

import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.Query;
import gr.iti.mklab.framework.client.search.SearchEngineResponse;

/**
 *
 * @author etzoannos
 */
public class SolrItemHandler implements SolrHandler<Item> {

    private Logger logger = Logger.getLogger(SolrItemHandler.class);

    private SolrServer server;
    
    private static Map<String, SolrItemHandler> INSTANCES = new HashMap<String, SolrItemHandler>();
    private static int commitPeriod = 10000;

    private SolrItemHandler(String collection) throws Exception {
        server = new HttpSolrServer(collection);
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

    public boolean insert(Item item) {
        boolean status = true;
        try {
            SolrItem solrItem = new SolrItem(item);
            server.addBean(solrItem, commitPeriod);
        } 
        catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (Exception ex) {
        	logger.error(ex.getMessage());
            status = false;
        } 
       
        return status;
    }

    public boolean insert(List<Item> items) {
        boolean status = true;
        try {
            List<SolrItem> solrItems = new ArrayList<SolrItem>();
            for (Item item : items) {
                SolrItem solrItem = new SolrItem(item);
                solrItems.add(solrItem);
            }
            server.addBeans(solrItems, commitPeriod);
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            status = false;
        }
        
        return status;
    }

    public boolean delete(String itemId) {
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

    public boolean delete(Query query) {
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

    public Item get(String itemId) {
        SolrQuery solrQuery = new SolrQuery("id:" + itemId);
        solrQuery.setRows(1);
        SearchEngineResponse<Item> response = findWithoutFacet(solrQuery);
        List<Item> items = response.getResults();
        if (!items.isEmpty()) {
            return items.get(0);
        } else {
            logger.info("no iyem for this id found!!");
            return null;
        }
    }
    
    public SearchEngineResponse<Item> find(SolrQuery query) {

        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();
        QueryResponse rsp;
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        }

        response.setNumFound(rsp.getResults().getNumFound());
        List<SolrItem> solrItems = rsp.getBeans(SolrItem.class);
        if (solrItems != null) {
            logger.info("got: " + solrItems.size() + " items from Solr - total results: " + response.getNumFound());
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
        if (solrFacetList != null) {
            for (int i = 0; i < solrFacetList.size(); i++) {
                Facet facet = new Facet(); 
                List<Bucket> buckets = new ArrayList<Bucket>();
                FacetField solrFacet = solrFacetList.get(i); 
                List<FacetField.Count> values = solrFacet.getValues();
                String solrFacetName = solrFacet.getName();
                boolean validFacet = false;

                //populate Valid Facets
                for (int j = 0; j < solrFacet.getValueCount(); j++) {
                    Bucket bucket = new Bucket();
                    long bucketCount = values.get(j).getCount();
                    
                    validFacet = true; //facet contains at least one non-zero length bucket
                    bucket.setCount(bucketCount);
                    bucket.setName(values.get(j).getName());
                    bucket.setQuery(values.get(j).getAsFilterQuery());
                    bucket.setFacet(solrFacetName);
                    buckets.add(bucket);
                }
                
                if (validFacet) {
                    facet.setBuckets(buckets);
                    facet.setName(solrFacetName);
                    facets.add(facet);
                }
            }

            // Sort
            Collections.sort(facets, new Comparator<Facet>() {
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

    private SearchEngineResponse<Item> findWithoutFacet(SolrQuery query) {

        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();
        QueryResponse rsp;
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        }

        response.setNumFound(rsp.getResults().getNumFound());
        List<SolrItem> solrItems = rsp.getBeans(SolrItem.class);
        if (solrItems != null) {
            logger.info("got: " + solrItems.size() + " items from Solr - total results: " + response.getNumFound());
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

        return response;
    }

    public SearchEngineResponse<Item> findItems(String query, List<String> filters, List<String> facets, String orderBy, int size) {

        List<Item> items = new ArrayList<Item>();
        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();

        if (query == null || query.isEmpty() || query.equals("")) {
            return response;
        }
        
        query = "title:(" + query + ") OR description:(" + query + ")";

        //Set source filters in case they exist exist
        for (String filter : filters) {
            query += " AND " + filter;
        }

        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(size);

        //Set facets if necessary
        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);

        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        logger.info("Solr Query : " + query);
        response = find(solrQuery);
        if (response != null) {
            List<Item> results = response.getResults();

            for (Item it : results) {
                items.add(it);
                if ((items.size() >= size)) {
                    break;
                }
            }
        }
        response.setResults(items);
        
        
        return response;
    }

    public SearchEngineResponse<Item> findItems(Dysco dysco, List<String> filters, List<String> facets, String orderBy, int size) {

    	List<Item> items = new ArrayList<Item>();
        SearchEngineResponse<Item> response = new SearchEngineResponse<Item>();    	 

        // Create a Solr Query

        List<String> queryParts = new ArrayList<String>();
        
        List<String> words = dysco.getWords();
        if(words != null && !words.isEmpty()) {
        	String contentQuery = String.join(" OR ", words);
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
        
        List<String> wordsToExclude = dysco.getWordsToExclude();
        if (wordsToExclude != null && !wordsToExclude.isEmpty()) {
        	String exclude = StringUtils.join(wordsToExclude, " OR ");
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
        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);
        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }
        logger.info("Solr Query: " + query);

        response = find(solrQuery);
        if (response != null) {
            List<Item> results = response.getResults();

            for (Item it : results) {
                items.add(it);
                if (items.size() >= size) {
                    break;
                }
            }
        }

        response.setResults(items);
        return response;
    }


}
