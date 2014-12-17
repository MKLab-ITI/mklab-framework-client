package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.dysco.Dysco;
import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.SearchEngineResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author	Manos Schinas
 * @email	manosetro@iti.gr
 * 
 */
public class SolrMediaItemHandler implements SolrHandler<MediaItem> {

    private SolrServer server;
	private Logger logger;
    
	private static Map<String, SolrMediaItemHandler> INSTANCES = new HashMap<String, SolrMediaItemHandler>();
    private static int commitPeriod = 5000;

    // Private constructor prevents instantiation from other classes
    private SolrMediaItemHandler(String collection) throws Exception {
    	server = new HttpSolrServer(collection);
    	logger = Logger.getLogger(SolrMediaItemHandler.class);
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

    public boolean insert(MediaItem item) {
        boolean status = true;
        try {
            SolrMediaItem solrItem = new SolrMediaItem(item);
            server.addBean(solrItem, commitPeriod);
        } catch (Exception e) {
            logger.error(e.getMessage());
            status = false;
        } 
   
        return status;
    }

    public boolean insert(List<MediaItem> mediaItems) {

        boolean status = true;
        try {
            List<SolrMediaItem> solrMediaItems = new ArrayList<SolrMediaItem>();
            for (MediaItem mediaItem : mediaItems) {
                SolrMediaItem solrMediaItem = new SolrMediaItem(mediaItem);
                solrMediaItems.add(solrMediaItem);
            }
            server.addBeans(solrMediaItems, commitPeriod);
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            status = false;
        }
            
        return status;
    }

    public boolean deleteById(String id) {
        boolean status = false;
        try {
        	String query = "id:" + id;
        	UpdateResponse response  = server.deleteByQuery(query, commitPeriod);
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

    public MediaItem get(String id) {

        SolrQuery solrQuery = new SolrQuery("id:" + id);
        SearchEngineResponse<MediaItem> mi = find(solrQuery);

        List<MediaItem> results = mi.getResults();

        if (results == null || results.size() == 0) {
            return null;
        }

        MediaItem mediaItem = results.get(0);
        mediaItem.setId(id);
        return mediaItem;

    }

    
    public SearchEngineResponse<MediaItem> find(SolrQuery query) {

    	//query.set("qt","/socialsearch");
    	
        SearchEngineResponse<MediaItem> response = new SearchEngineResponse<MediaItem>();
        QueryResponse rsp;
        try {
            rsp = server.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        }
        response.setNumFound(rsp.getResults().getNumFound());
       
        List<SolrMediaItem> solrItems = new ArrayList<SolrMediaItem>();
        
        SolrDocumentList docs = rsp.getResults();
        for(SolrDocument doc : docs) {
        	SolrMediaItem solrMediaItem = new SolrMediaItem(doc);
        	solrItems.add(solrMediaItem);
        }
      
        if (solrItems != null) {
            logger.info("got: " + solrItems.size() + " media items from Solr - total results: " + response.getNumFound());
        }
        
        List<MediaItem> mediaItems = new ArrayList<MediaItem>();
        for (SolrMediaItem solrMediaItem : solrItems) {
            try {
                MediaItem mediaItem = solrMediaItem.toMediaItem();
                String id = mediaItem.getId();
               
                mediaItem.setId(id);

                mediaItems.add(mediaItem);
            } catch (MalformedURLException ex) {
                logger.error(ex.getMessage());
            }
        }

        response.setResults(mediaItems);
        List<Facet> facets = new ArrayList<Facet>();
        List<FacetField> solrFacetList = rsp.getFacetFields();
        if (solrFacetList != null) {

            //populate all non-zero facets

            for (int i = 0; i < solrFacetList.size(); i++) {

                Facet facet = new Facet();
                List<Bucket> buckets = new ArrayList<Bucket>();
                FacetField solrFacet = solrFacetList.get(i); //get the ones returned from Solr
                List<FacetField.Count> values = solrFacet.getValues();
                String solrFacetName = solrFacet.getName();
                boolean validFacet = false;

                for (int j = 0; j < solrFacet.getValueCount(); j++) {

                    Bucket bucket = new Bucket();
                    long bucketCount = values.get(j).getCount();
                    validFacet = true; 
                    bucket.setCount(bucketCount);
                    bucket.setName(values.get(j).getName());
                    bucket.setQuery(values.get(j).getAsFilterQuery());
                    bucket.setFacet(solrFacetName);
                    buckets.add(bucket);
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
    
    public SearchEngineResponse<MediaItem> findMediaItems(String query, List<String> filters, List<String> facets, String orderBy, int size) {

        List<MediaItem> mediaItems = new LinkedList<MediaItem>();
        SearchEngineResponse<MediaItem> response = new SearchEngineResponse<MediaItem>();

        if (query == null || query.equals("")) {
            return response;
        }

        query = "((title : " + query + ") OR (description:" + query + "))";
        
        //Set filters in case they exist exist
        for (String filter : filters) {
            query += " AND " + filter;
        }

        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(size);

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
            List<MediaItem> results = response.getResults();
            Set<String> urls = new HashSet<String>();
            Set<String> clusterIds = new HashSet<String>();
            for (MediaItem mi : results) {
                if (!urls.contains(mi.getUrl())) {
                	String clusterId = mi.getClusterId();
                	if(clusterId == null) {
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                	else if(!clusterIds.contains(clusterId)) {
                		clusterIds.add(clusterId);
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                }

                if ((mediaItems.size() >= size)) {
                    break;
                }
            }
        }

        response.setResults(mediaItems);
        return response;
    }

    public SearchEngineResponse<MediaItem> findMediaItems(Dysco dysco, List<String> filters, List<String> facets, String orderBy, int size) {

        List<MediaItem> mediaItems = new ArrayList<MediaItem>();
        SearchEngineResponse<MediaItem> response = new SearchEngineResponse<MediaItem>();

        List<String> queryParts = new ArrayList<String>();
        
        List<String> words = dysco.getWords();
        if(words != null && !words.isEmpty()) {
        String contentQuery = StringUtils.join(words, " OR ");
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
        
        if (response != null) {
            List<MediaItem> results = response.getResults();
            Set<String> urls = new HashSet<String>();
            Set<String> clusterIds = new HashSet<String>();
            for (MediaItem mi : results) {

                if (!urls.contains(mi.getUrl())) {
                	String clusterId = mi.getClusterId();
                	if(clusterId == null) {
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                	else if(!clusterIds.contains(clusterId)) {
                		clusterIds.add(clusterId);
                		urls.add(mi.getUrl());
                		mediaItems.add(mi);	
                	}
                }

                if ((mediaItems.size() >= size)) {
                    break;
                }
            }
        }

        response.setResults(mediaItems);
        return response;
    }
    
}